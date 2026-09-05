package com.sanedge.transfer.service.impl;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.enums.Status;
import com.sanedge.common.exception.ResourceNotFoundException;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.transfer.domain.requests.CreateTransferRequest;
import com.sanedge.transfer.domain.requests.UpdateTransferRequest;
import com.sanedge.transfer.domain.response.TransferResponse;
import com.sanedge.transfer.domain.response.TransferResponseDeleteAt;
import com.sanedge.transfer.entity.Transfer;
import com.sanedge.transfer.repository.TransferCommandRepository;
import com.sanedge.transfer.repository.TransferQueryRepository;
import com.sanedge.transfer.service.TransferCommandService;

import io.opentelemetry.api.common.Attributes;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import pb.card.CardQueryService;
import pb.saldo.SaldoCommandService;
import pb.saldo.SaldoQueryService;
import com.sanedge.transfer.service.KafkaService;
import io.vertx.core.json.JsonObject;
import pb.card.Card.CardWithEmailResponse;

@ApplicationScoped
public class TransferCommandServiceImpl implements TransferCommandService {
    private static final Logger logger = LoggerFactory.getLogger(TransferCommandServiceImpl.class);

    private final TransferCommandRepository transferCommandRepository;
    private final CardQueryService cardQueryService;
    private final SaldoQueryService saldoQueryService;
    private final SaldoCommandService saldoCommandService;
    private final TransferQueryRepository transferQueryRepository;
    private final Validator validator;
    private final RedisService redisService;
    private final KafkaService kafkaService;
    private final TracingMetrics tracingMetrics;

    @Inject
    public TransferCommandServiceImpl(TransferCommandRepository transferCommandRepository,
            @GrpcClient("card") CardQueryService cardQueryService,
            @GrpcClient("saldo") SaldoQueryService saldoQueryService,
            @GrpcClient("saldo") SaldoCommandService saldoCommandService,
            TransferQueryRepository transferQueryRepository,
            Validator validator,
            RedisService redisService,
            KafkaService kafkaService,
            TracingMetrics tracingMetrics) {
        this.transferCommandRepository = transferCommandRepository;
        this.cardQueryService = cardQueryService;
        this.saldoQueryService = saldoQueryService;
        this.saldoCommandService = saldoCommandService;
        this.transferQueryRepository = transferQueryRepository;
        this.validator = validator;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.tracingMetrics = tracingMetrics;
    }

    private <T> boolean validateRequest(T req) {
        Set<ConstraintViolation<T>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("; ");
            }
            logger.error("Validation failed: {}", sb);
            return false;
        }
        return true;
    }

    private Uni<Void> evictCaches(String senderCard, String receiverCard, Long transferId) {
        String key1 = "saldo:card:" + senderCard;
        String key2 = "saldo:card:" + receiverCard;
        String key3 = "transfers:from:" + senderCard;
        String key4 = "transfers:to:" + receiverCard;
        String key5 = "transfers:id:" + transferId;

        return Uni.combine().all().unis(
                redisService.deleteReactive(key1),
                redisService.deleteReactive(key2),
                redisService.deleteReactive(key3),
                redisService.deleteReactive(key4),
                redisService.deleteReactive(key5)).discardItems();
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TransferResponse>> create(CreateTransferRequest req) {
        logger.info("🚀 Starting create transfer: {}", req);

        if (!validateRequest(req)) {
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        Attributes attrs = Attributes.builder()
                .put("operation", "create_transfer")
                .put("transferFrom", req.getTransferFrom())
                .put("transferTo", req.getTransferTo())
                .build();

        return tracingMetrics.traceAndMeasure("createTransfer", "create_transfer", attrs, () -> {
            final String[] senderEmailContainer = new String[1];
            return cardQueryService
                    .findUserCardByCardNumber(
                            pb.card.Card.FindByCardNumberRequest.newBuilder().setCardNumber(req.getTransferFrom()).build())
                    .chain(senderCardResponse -> {
                        if (senderCardResponse == null || senderCardResponse.getCardNumber() == null || senderCardResponse.getCardNumber().isEmpty()) {
                            logger.error("❌ sender card {} not found", req.getTransferFrom());
                            throw new ResourceNotFoundException("Sender card not found");
                        }
                        senderEmailContainer[0] = senderCardResponse.getEmail();
                        return cardQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                                .setCardNumber(req.getTransferTo()).build());
                    })
                    .chain(receiverCardResponse -> {
                        if (receiverCardResponse == null || !receiverCardResponse.hasData()) {
                            logger.error("❌ receiver card {} not found", req.getTransferTo());
                            throw new ResourceNotFoundException("Receiver card not found");
                        }
                        return saldoQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                                .setCardNumber(req.getTransferFrom()).build());
                    })
                    .chain(senderSaldoResponse -> {
                        if (senderSaldoResponse == null || !senderSaldoResponse.hasData()) {
                            logger.error("❌ failed to fetch sender saldo");
                            throw new ResourceNotFoundException("Sender saldo not found");
                        }
                        pb.saldo.Saldo.SaldoResponse senderSaldo = senderSaldoResponse.getData();
                        return saldoQueryService
                                .findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                                        .setCardNumber(req.getTransferTo()).build())
                                .map(receiverSaldoResponse -> {
                                    if (receiverSaldoResponse == null || !receiverSaldoResponse.hasData()) {
                                        logger.error("❌ failed to fetch receiver saldo");
                                        throw new ResourceNotFoundException("Receiver saldo not found");
                                    }
                                    pb.saldo.Saldo.SaldoResponse receiverSaldo = receiverSaldoResponse.getData();

                                    if (senderSaldo.getTotalBalance() < req.getTransferAmount()) {
                                        logger.error("❌ insufficient balance, requested={}, available={}",
                                                req.getTransferAmount(), senderSaldo.getTotalBalance());
                                        throw new IllegalStateException("Insufficient balance");
                                    }

                                    return new SaldoPair(senderSaldo, receiverSaldo);
                                });
                    })
                    .chain(pair -> {
                        Transfer transferEntity = new Transfer();
                        transferEntity.setTransferNo(UUID.randomUUID());
                        transferEntity.setTransferFrom(req.getTransferFrom());
                        transferEntity.setTransferAmount(req.getTransferAmount().intValue());
                        transferEntity.setTransferTo(req.getTransferTo());
                        transferEntity.setTransferTime(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        transferEntity.setStatus(Status.PENDING);
                        transferEntity.setCreatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        transferEntity.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

                        return transferCommandRepository.persist(transferEntity)
                                .chain(savedTransfer -> {
                                    int newSenderBalance = pair.sender.getTotalBalance()
                                            - req.getTransferAmount().intValue();
                                    int newReceiverBalance = pair.receiver.getTotalBalance()
                                            + req.getTransferAmount().intValue();

                                    return saldoCommandService
                                            .updateSaldoBalance(pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                    .setCardNumber(pair.sender.getCardNumber())
                                                    .setTotalBalance(newSenderBalance)
                                                    .build())
                                            .chain(v -> saldoCommandService.updateSaldoBalance(
                                                    pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                            .setCardNumber(pair.receiver.getCardNumber())
                                                            .setTotalBalance(newReceiverBalance)
                                                            .build()))
                                            .chain(v -> transferCommandRepository
                                                    .updateTransferStatus(savedTransfer.getTransferId(), "SUCCESS"))
                                            .chain(updatedTransfer -> {
                                                logger.info("✅ Transfer created successfully with ID={}",
                                                        updatedTransfer.getTransferId());
                                                return evictCaches(req.getTransferFrom(), req.getTransferTo(),
                                                        updatedTransfer.getTransferId())
                                                        .chain(x -> {
                                                            Uni<Void> emailUni = Uni.createFrom().voidItem();
                                                            String email = senderEmailContainer[0];
                                                            if (email != null && !email.isEmpty()) {
                                                                String emailSubject = "Transfer Successful - SanEdge";
                                                                String emailBody = String.format(
                                                                        "Hello,\n\nYour transfer of %d to %s has been processed successfully.\n\nRegards,\nSupport Team",
                                                                        req.getTransferAmount().intValue(),
                                                                        req.getTransferTo()
                                                                );

                                                                JsonObject emailPayload = new JsonObject()
                                                                        .put("email", email)
                                                                        .put("subject", emailSubject)
                                                                        .put("body", emailBody);

                                                                emailUni = kafkaService.sendMessage("email-service-topic-transfer-create", String.valueOf(updatedTransfer.getTransferId()), emailPayload)
                                                                        .onFailure().recoverWithNull();
                                                            }

                                                            return emailUni.map(v3 -> ApiResponse.success("Transfer created successfully",
                                                                    TransferResponse.from(updatedTransfer)));
                                                        });
                                            });
                                });
                    })
                    .onFailure().recoverWithItem(e -> {
                        logger.error("❌ Failed to create transfer", e);
                        return new ApiResponse<>("error", "Failed to create transfer: " + e.getMessage(), null);
                    });
        });
    }

    private static class SaldoPair {
        final pb.saldo.Saldo.SaldoResponse sender;
        final pb.saldo.Saldo.SaldoResponse receiver;

        SaldoPair(pb.saldo.Saldo.SaldoResponse sender, pb.saldo.Saldo.SaldoResponse receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TransferResponse>> update(UpdateTransferRequest req) {
        logger.info("🔄 Starting update transfer process: {}", req);

        if (!validateRequest(req)) {
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        if (req.getTransferId() == null || req.getTransferId() <= 0) {
            return Uni.createFrom().item(new ApiResponse<>("error", "transferId is required", null));
        }

        Attributes attrs = Attributes.builder()
                .put("operation", "update_transfer")
                .put("transferId", String.valueOf(req.getTransferId()))
                .build();

        return tracingMetrics.traceAndMeasure("updateTransfer", "update_transfer", attrs, () -> {
            return transferQueryRepository.findTransferById(req.getTransferId())
                    .chain(transfer -> {
                        if (transfer == null) {
                            logger.error("❌ Transfer {} not found", req.getTransferId());
                            throw new ResourceNotFoundException("Transfer " + req.getTransferId() + " not found");
                        }

                        long amountDifference = req.getTransferAmount() - transfer.getTransferAmount();

                        return saldoQueryService
                                .findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                                        .setCardNumber(transfer.getTransferFrom()).build())
                                .chain(senderSaldoResponse -> {
                                    if (senderSaldoResponse == null || !senderSaldoResponse.hasData()) {
                                        return transferCommandRepository.updateTransferStatus(req.getTransferId(), "FAILED")
                                                .chain(v -> {
                                                    throw new ResourceNotFoundException(
                                                            "Sender card " + transfer.getTransferFrom() + " not found");
                                                });
                                    }
                                    pb.saldo.Saldo.SaldoResponse senderSaldo = senderSaldoResponse.getData();

                                    long newSenderBalance = senderSaldo.getTotalBalance() - amountDifference;
                                    if (newSenderBalance < 0) {
                                        logger.error("❌ Insufficient balance for sender {}", transfer.getTransferFrom());
                                        return transferCommandRepository.updateTransferStatus(req.getTransferId(), "FAILED")
                                                .chain(v -> {
                                                    throw new IllegalStateException("Insufficient balance");
                                                });
                                    }

                                    return saldoQueryService
                                            .findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                                                    .setCardNumber(transfer.getTransferTo()).build())
                                            .chain(receiverSaldoResponse -> {
                                                if (receiverSaldoResponse == null || !receiverSaldoResponse.hasData()) {
                                                    return transferCommandRepository
                                                            .updateTransferStatus(req.getTransferId(), "FAILED")
                                                            .chain(v -> {
                                                                throw new ResourceNotFoundException("Receiver card "
                                                                        + transfer.getTransferTo() + " not found");
                                                            });
                                                }
                                                pb.saldo.Saldo.SaldoResponse receiverSaldo = receiverSaldoResponse
                                                        .getData();

                                                long newReceiverBalance = receiverSaldo.getTotalBalance()
                                                        + amountDifference;

                                                return saldoCommandService
                                                        .updateSaldoBalance(
                                                                pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                                        .setCardNumber(senderSaldo.getCardNumber())
                                                                        .setTotalBalance((int) newSenderBalance)
                                                                        .build())
                                                        .chain(v -> saldoCommandService.updateSaldoBalance(
                                                                pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                                        .setCardNumber(receiverSaldo.getCardNumber())
                                                                        .setTotalBalance((int) newReceiverBalance)
                                                                        .build()))
                                                        .chain(v -> {
                                                            transfer.setTransferAmount(req.getTransferAmount().intValue());
                                                            transfer.setTransferFrom(req.getTransferFrom());
                                                            transfer.setTransferTo(req.getTransferTo());
                                                            transfer.setUpdatedAt(java.sql.Timestamp
                                                                    .valueOf(java.time.LocalDateTime.now()));
                                                            return transferCommandRepository.persist(transfer);
                                                        })
                                                        .chain(updatedTransfer -> transferCommandRepository
                                                                .updateTransferStatus(req.getTransferId(), "SUCCESS"))
                                                        .chain(finalTransfer -> {
                                                            logger.info("✅ Successfully updated transfer {}",
                                                                    req.getTransferId());
                                                            return evictCaches(transfer.getTransferFrom(),
                                                                    transfer.getTransferTo(), finalTransfer.getTransferId())
                                                                    .map(x -> ApiResponse.success(
                                                                            "Transfer updated successfully",
                                                                            TransferResponse.from(finalTransfer)));
                                                        });
                                            });
                                });
                    })
                    .onFailure().recoverWithItem(e -> {
                        logger.error("❌ Failed to update transfer {}", req.getTransferId(), e);
                        return new ApiResponse<>("error", "Failed to update transfer: " + e.getMessage(), null);
                    });
        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TransferResponseDeleteAt>> trashed(Long transferId) {
        logger.info("🗑️ Trashing transfer id={}", transferId);

        return transferCommandRepository.trashed(transferId)
                .chain(transfer -> {
                    if (transfer == null) {
                        throw new ResourceNotFoundException("Transfer not found with ID " + transferId);
                    }
                    return evictCaches(transfer.getTransferFrom(), transfer.getTransferTo(), transfer.getTransferId())
                            .map(x -> ApiResponse.success("🗑️ Transfer trashed successfully!",
                                    TransferResponseDeleteAt.from(transfer)));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to trash transfer id={}", transferId, e);
                    return new ApiResponse<>("error", "Failed to trash transfer: " + e.getMessage(), null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TransferResponseDeleteAt>> restore(Long transferId) {
        logger.info("♻️ Restoring transfer id={}", transferId);

        return transferCommandRepository.restore(transferId)
                .chain(transfer -> {
                    if (transfer == null) {
                        throw new ResourceNotFoundException("Transfer not found with ID " + transferId);
                    }
                    return evictCaches(transfer.getTransferFrom(), transfer.getTransferTo(), transfer.getTransferId())
                            .map(x -> ApiResponse.success("♻️ Transfer restored successfully!",
                                    TransferResponseDeleteAt.from(transfer)));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore transfer id={}", transferId, e);
                    return new ApiResponse<>("error", "Failed to restore transfer: " + e.getMessage(), null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deletePermanent(Long transferId) {
        logger.info("🧨 Permanently deleting transfer id={}", transferId);

        return transferQueryRepository.findTransferById(transferId)
                .chain(transfer -> {
                    if (transfer == null) {
                        return Uni.createFrom().item(ApiResponse.success("🧨 Transfer permanently deleted!", true));
                    }
                    return transferCommandRepository.deletePermanent(transferId)
                            .chain(success -> evictCaches(transfer.getTransferFrom(), transfer.getTransferTo(),
                                    transfer.getTransferId())
                                    .map(x -> ApiResponse.success("🧨 Transfer permanently deleted!", success)));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to permanently delete transfer id={}", transferId, e);
                    return new ApiResponse<>("error", "💥 Failed to permanently delete transfer: " + e.getMessage(),
                            false);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> restoreAll() {
        logger.info("🔄 Restoring ALL trashed transfers");

        return transferCommandRepository.restoreAllDeleted()
                .map(success -> ApiResponse.success("🔄 All transfers restored successfully!", success))
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore all transfers", e);
                    return new ApiResponse<>("error", "💥 Failed to restore all transfers: " + e.getMessage(), false);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteAll() {
        logger.info("💣 Permanently deleting ALL trashed transfers");

        return transferCommandRepository.deleteAllDeleted()
                .map(success -> ApiResponse.success("💣 All transfers permanently deleted!", success))
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to delete all transfers", e);
                    return new ApiResponse<>("error", "💥 Failed to delete all transfers: " + e.getMessage(), false);
                });
    }
}
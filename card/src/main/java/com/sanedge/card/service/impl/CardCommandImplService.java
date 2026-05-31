package com.sanedge.card.service.impl;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.card.domain.requests.CreateCardRequest;
import com.sanedge.card.domain.requests.UpdateCardRequest;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.CardResponse;
import com.sanedge.card.domain.response.CardResponseDeleteAt;
import com.sanedge.card.entity.Card;
import pb.user.UserQueryService;
import pb.user.User.FindByIdUserRequest;
import io.quarkus.grpc.GrpcClient;
import com.sanedge.card.service.KafkaService;
import io.vertx.core.json.JsonObject;
import com.sanedge.card.repository.CardCommandRepository;
import com.sanedge.card.repository.CardQueryRepository;
import com.sanedge.card.service.CardCommandService;
import com.sanedge.common.utils.CardNumberGenerator;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ApplicationScoped
public class CardCommandImplService implements CardCommandService {
    private static final Logger logger = LoggerFactory.getLogger(CardCommandImplService.class);

    @Inject
    CardCommandRepository cardCommandRepository;

    @Inject
    CardQueryRepository cardQueryRepository;

    @Inject
    @GrpcClient("user")
    UserQueryService userQueryService;

    @Inject
    KafkaService kafkaService;

    @Inject
    Validator validator;

    @Override
    @WithTransaction
    public Uni<ApiResponse<CardResponse>> createCard(CreateCardRequest req) {
        logger.info("🆕 Creating card for user_id={}", req.getUserId());

        if (!validateRequest(req)) {
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        if (req.getUserId() == null) {
            logger.error("user_id is required");
            return Uni.createFrom().item(new ApiResponse<>("error", "user_id is required", null));
        }

        return userQueryService.findById(FindByIdUserRequest.newBuilder().setId(req.getUserId().intValue()).build())
                .chain(response -> {
                    if (response == null || !response.hasData()) {
                        logger.error("👤 User with id {} not found", req.getUserId());
                        throw new IllegalArgumentException("User not found");
                    }

                    Card card = new Card();
                    try {
                        String cardNumber = CardNumberGenerator.randomCardNumber();
                        card.setCardNumber(cardNumber);
                    } catch (Exception e) {
                        logger.error("❌ Failed to generate card number", e);
                        throw new RuntimeException("Failed to generate card number");
                    }

                    card.setUserId(req.getUserId().intValue());
                    card.setCardType(req.getCardType());
                    card.setExpireDate(java.sql.Date.valueOf(req.getExpireDate()));
                    card.setCvv(req.getCvv());
                    card.setCardProvider(req.getCardProvider());

                    return cardCommandRepository.persist(card)
                            .chain(savedCard -> {
                                JsonObject saldoPayload = new JsonObject()
                                        .put("card_number", savedCard.getCardNumber())
                                        .put("total_balance", 0);
                                return kafkaService.sendMessage(
                                        "saldo-service-topic-create-saldo",
                                        savedCard.getCardNumber(),
                                        saldoPayload)
                                        .map(v -> savedCard);
                            })
                            .map(savedCard -> {
                                CardResponse cardResp = CardResponse.from(savedCard);
                                logger.info("✅ Card created successfully with card_id={}", cardResp.getId());
                                return new ApiResponse<>("success", "Card created successfully", cardResp);
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to create card for user_id={}", req.getUserId(), e);
                    String msg = "Failed to create card";
                    if (e instanceof IllegalArgumentException) {
                        msg = e.getMessage();
                    }
                    return new ApiResponse<>("error", msg, null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<CardResponse>> updateCard(UpdateCardRequest req) {
        logger.info("🔄 Updating card id={} for user_id={}", req.getCardId(), req.getUserId());

        if (!validateRequest(req)) {
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        if (req.getCardId() == null) {
            logger.error("card_id is required");
            return Uni.createFrom().item(new ApiResponse<>("error", "card_id is required", null));
        }

        if (req.getUserId() == null) {
            logger.error("user_id is required");
            return Uni.createFrom().item(new ApiResponse<>("error", "user_id is required", null));
        }

        return userQueryService.findById(FindByIdUserRequest.newBuilder().setId(req.getUserId().intValue()).build())
                .chain(response -> {
                    if (response == null || !response.hasData()) {
                        logger.error("👤 User with id {} not found", req.getUserId());
                        throw new IllegalArgumentException("User not found");
                    }
                    return cardQueryRepository.findById(req.getCardId());
                })
                .chain(card -> {
                    if (card == null) {
                        logger.error("💳 Card with id {} not found", req.getCardId());
                        throw new IllegalArgumentException("Card not found");
                    }

                    card.setCardType(req.getCardType());
                    card.setExpireDate(java.sql.Date.valueOf(req.getExpireDate()));
                    card.setCvv(req.getCvv());
                    card.setCardProvider(req.getCardProvider());

                    return cardCommandRepository.persist(card)
                            .map(updatedCard -> {
                                CardResponse response = CardResponse.from(updatedCard);
                                logger.info("✅ Card updated successfully with card_id={}", response.getId());
                                return new ApiResponse<>("success", "Card updated successfully", response);
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to update card id={} for user_id={}", req.getCardId(), req.getUserId(), e);
                    String msg = "Failed to update card";
                    if (e instanceof IllegalArgumentException) {
                        msg = e.getMessage();
                    }
                    return new ApiResponse<>("error", msg, null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<CardResponseDeleteAt>> trashCard(Long id) {
        logger.info("🗑️ Trashing card id={}", id);
        return cardCommandRepository.trashed(id)
                .map(card -> {
                    if (card == null) {
                        throw new IllegalArgumentException("Card not found");
                    }
                    return new ApiResponse<>("success", "Card trashed successfully", CardResponseDeleteAt.from(card));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to trash card id={}", id, e);
                    return new ApiResponse<>("error", "Failed to trash card", null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<CardResponseDeleteAt>> restoreCard(Long id) {
        logger.info("♻️ Restoring card id={}", id);
        return cardCommandRepository.restore(id)
                .map(card -> {
                    if (card == null) {
                        throw new IllegalArgumentException("Card not found");
                    }
                    return new ApiResponse<>("success", "Card restored successfully", CardResponseDeleteAt.from(card));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore card id={}", id, e);
                    return new ApiResponse<>("error", "Failed to restore card", null);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteCard(Long id) {
        logger.info("🧨 Permanently deleting card id={}", id);
        return cardCommandRepository.deletePermanent(id)
                .map(deleted -> new ApiResponse<>("success", "Card permanently deleted", deleted))
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to permanently delete card id={}", id, e);
                    return new ApiResponse<>("error", "Failed to permanently delete card", false);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> restoreAll() {
        logger.info("🔄 Restoring ALL trashed cards");
        return cardCommandRepository.restoreAllDeleted()
                .map(restored -> new ApiResponse<>("success", "All cards restored successfully", restored))
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore all cards", e);
                    return new ApiResponse<>("error", "Failed to restore all cards", false);
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteAll() {
        logger.info("💣 Permanently deleting ALL trashed cards");
        return cardCommandRepository.deleteAllDeleted()
                .map(deleted -> new ApiResponse<>("success", "All cards permanently deleted", deleted))
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to delete all cards", e);
                    return new ApiResponse<>("error", "Failed to delete all cards", false);
                });
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
}

package com.sanedge.topup.service.impl;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.common.config.RedisService;
import com.sanedge.topup.domain.requests.CreateTopupRequest;
import com.sanedge.topup.domain.requests.UpdateTopupRequest;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.TopupResponse;
import com.sanedge.topup.domain.response.TopupResponseDeleteAt;
import com.sanedge.topup.entity.Topup;
import com.sanedge.common.enums.Status;
import com.sanedge.common.exception.ResourceNotFoundException;
import pb.card.CardQueryService;
import pb.saldo.SaldoQueryService;
import pb.saldo.SaldoCommandService;
import io.quarkus.grpc.GrpcClient;
import com.sanedge.topup.service.KafkaService;
import io.vertx.core.json.JsonObject;
import com.sanedge.topup.repository.TopupCommandRepository;
import com.sanedge.topup.repository.TopupQueryRepository;
import com.sanedge.topup.service.TopupCommandService;

import io.opentelemetry.api.OpenTelemetry;
import com.sanedge.common.observability.TracingMetrics;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@ApplicationScoped
public class TopupCommandServiceImpl implements TopupCommandService {
    private static final Logger logger = LoggerFactory.getLogger(TopupCommandServiceImpl.class);

    private final CardQueryService cardQueryService;
    private final SaldoQueryService saldoQueryService;
    private final SaldoCommandService saldoCommandService;
    private final TopupQueryRepository topupQueryRepository;
    private final TopupCommandRepository topupCommandRepository;
    private final Validator validator;
    private final RedisService redisService;
    private final KafkaService kafkaService;

    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestDurationSeconds;

    @Inject
    public TopupCommandServiceImpl(@GrpcClient("card") CardQueryService cardQueryService,
                                  @GrpcClient("saldo") SaldoQueryService saldoQueryService,
                                  @GrpcClient("saldo") SaldoCommandService saldoCommandService,
                                  TopupQueryRepository topupQueryRepository,
                                  TopupCommandRepository topupCommandRepository,
                                  Validator validator,
                                  OpenTelemetry openTelemetry,
                                  RedisService redisService,
                                  KafkaService kafkaService) {
        this.cardQueryService = cardQueryService;
        this.saldoQueryService = saldoQueryService;
        this.saldoCommandService = saldoCommandService;
        this.topupQueryRepository = topupQueryRepository;
        this.topupCommandRepository = topupCommandRepository;
        this.validator = validator;
        this.redisService = redisService;
        this.kafkaService = kafkaService;
        this.tracer = openTelemetry.getTracer("topup-command-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("topup-command-service");

        this.requestsTotal = meter.counterBuilder("requests_total")
                .setDescription("Total number of requests")
                .build();
        this.requestDurationSeconds = meter.histogramBuilder("request_duration_seconds")
                .setDescription("Request duration in seconds")
                .setUnit("s")
                .build();
    }

    // Test-friendly overload: (services..., validator, redis, kafka, tracingMetrics) delegating
    // to the @Inject constructor with a no-op OpenTelemetry (tracing stays inert under unit tests).
    public TopupCommandServiceImpl(@GrpcClient("card") CardQueryService cardQueryService,
                                  @GrpcClient("saldo") SaldoQueryService saldoQueryService,
                                  @GrpcClient("saldo") SaldoCommandService saldoCommandService,
                                  TopupQueryRepository topupQueryRepository,
                                  TopupCommandRepository topupCommandRepository,
                                  Validator validator,
                                  RedisService redisService,
                                  KafkaService kafkaService,
                                  TracingMetrics tracingMetrics) {
        this(cardQueryService, saldoQueryService, saldoCommandService,
                topupQueryRepository, topupCommandRepository, validator,
                OpenTelemetry.noop(), redisService, kafkaService);
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

    @Override
    @WithTransaction
    public Uni<ApiResponse<TopupResponse>> create(CreateTopupRequest req) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("createTopup")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "create_topup")
                .setAttribute("cardNumber", req.getCardNumber())
                .startSpan();

        logger.info("🚀 Starting CreateTopup: {}", req);

        if (!validateRequest(req)) {
            span.setStatus(StatusCode.ERROR, "Validation failed");
            span.end();
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        return cardQueryService.findUserCardByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder().setCardNumber(req.getCardNumber()).build())
                .chain(cardWithEmail -> {
                    if (cardWithEmail == null || cardWithEmail.getCardNumber() == null || cardWithEmail.getCardNumber().isEmpty()) {
                        logger.error("❌ Card not found: {}", req.getCardNumber());
                        throw new ResourceNotFoundException("Card not found");
                    }

                    Topup topup = new Topup();
                    topup.setTopupNo(UUID.randomUUID());
                    topup.setCardNumber(req.getCardNumber());
                    topup.setTopupAmount(req.getTopupAmount().intValue());
                    topup.setTopupMethod(req.getTopupMethod());
                    topup.setStatus(Status.PENDING);
                    topup.setTopupTime(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                    topup.setCreatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                    topup.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

                    return topupCommandRepository.persist(topup)
                            .chain(savedTopup -> {
                                return saldoQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder().setCardNumber(req.getCardNumber()).build())
                                        .chain(saldoResponse -> {
                                            if (saldoResponse == null || !saldoResponse.hasData()) {
                                                logger.error("❌ Saldo not found for card: {}", req.getCardNumber());
                                                return topupCommandRepository.updateTopupStatus(savedTopup.getTopupId(), "FAILED")
                                                        .chain(v -> {
                                                            throw new ResourceNotFoundException("Saldo not found");
                                                        });
                                            }

                                            int newBalance = saldoResponse.getData().getTotalBalance() + req.getTopupAmount().intValue();
                                            return saldoCommandService.updateSaldoBalance(pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                            .setCardNumber(saldoResponse.getData().getCardNumber())
                                                            .setTotalBalance(newBalance)
                                                            .build())
                                                    .chain(v -> {
                                                        savedTopup.setStatus(Status.SUCCESS);
                                                        savedTopup.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                                                        return topupCommandRepository.persist(savedTopup);
                                                    })
                                                    .chain(updatedTopup -> {
                                                        logger.info("✅ CreateTopup completed: card={} topup_amount={} new_balance={}",
                                                                req.getCardNumber(), req.getTopupAmount(), newBalance);

                                                        String topupCardCache = "topups:card:" + req.getCardNumber();
                                                        String topupIdCache = "topup:id:" + updatedTopup.getTopupId();
                                                        String saldoCardCache = "saldo:card:" + req.getCardNumber();
                                                        String saldoIdCache = "saldo:id:" + saldoResponse.getData().getSaldoId();

                                                        return Uni.combine().all().unis(
                                                                redisService.deleteReactive(topupCardCache),
                                                                redisService.deleteReactive(topupIdCache),
                                                                redisService.deleteReactive(saldoCardCache),
                                                                redisService.deleteReactive(saldoIdCache)
                                                        ).asTuple().chain(t -> {
                                                            Uni<Void> emailUni = Uni.createFrom().voidItem();
                                                            if (cardWithEmail.getEmail() != null && !cardWithEmail.getEmail().isEmpty()) {
                                                                String emailSubject = "Topup Successful - SanEdge";
                                                                String emailBody = String.format(
                                                                        "Hello,\n\nYour topup of %d has been processed successfully.\n\nRegards,\nSupport Team",
                                                                        req.getTopupAmount().intValue()
                                                                );

                                                                JsonObject emailPayload = new JsonObject()
                                                                        .put("email", cardWithEmail.getEmail())
                                                                        .put("subject", emailSubject)
                                                                        .put("body", emailBody);

                                                                emailUni = kafkaService.sendMessage("email-service-topic-topup-create", String.valueOf(updatedTopup.getTopupId()), emailPayload)
                                                                        .onFailure().recoverWithNull();
                                                            }

                                                            return emailUni.map(v -> {
                                                                span.setStatus(StatusCode.OK);
                                                                requestsTotal.add(1, Attributes.of(
                                                                        AttributeKey.stringKey("operation"), "create_topup",
                                                                        AttributeKey.stringKey("status"), "success"));

                                                                return ApiResponse.success("Topup created successfully for card=" + req.getCardNumber(), TopupResponse.from(updatedTopup));
                                                            });
                                                        });
                                                    });
                                        });
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to create topup for card={}", req.getCardNumber(), e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "create_topup",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), null);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "create_topup"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TopupResponse>> update(UpdateTopupRequest req) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("updateTopup")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "update_topup")
                .setAttribute("topupId", String.valueOf(req.getTopupId()))
                .setAttribute("cardNumber", req.getCardNumber())
                .startSpan();

        logger.info("🚀 Starting UpdateTopup: {}", req);

        if (!validateRequest(req)) {
            span.setStatus(StatusCode.ERROR, "Validation failed");
            span.end();
            return Uni.createFrom().item(new ApiResponse<>("error", "Validation failed", null));
        }

        Long topupId = req.getTopupId();
        if (topupId == null) {
            logger.error("topup_id is required");
            span.setStatus(StatusCode.ERROR, "topup_id is required");
            span.end();
            return Uni.createFrom().item(new ApiResponse<>("error", "topup_id is required", null));
        }

        return cardQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder().setCardNumber(req.getCardNumber()).build())
                .chain(cardResponse -> {
                    if (cardResponse == null || !cardResponse.hasData()) {
                        logger.error("❌ Card not found: {}", req.getCardNumber());
                        return topupCommandRepository.updateTopupStatus(topupId, "FAILED")
                                .chain(v -> {
                                    throw new ResourceNotFoundException("Card not found");
                                });
                    }
                    return topupQueryRepository.findTopupById(topupId);
                })
                .chain(existingTopup -> {
                    if (existingTopup == null) {
                        logger.error("❌ Topup {} not found", topupId);
                        return topupCommandRepository.updateTopupStatus(topupId, "FAILED")
                                .chain(v -> {
                                    throw new ResourceNotFoundException("Topup not found");
                                });
                    }

                    int difference = req.getTopupAmount().intValue() - existingTopup.getTopupAmount();

                    return saldoQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder().setCardNumber(req.getCardNumber()).build())
                            .chain(saldoResponse -> {
                                if (saldoResponse == null || !saldoResponse.hasData()) {
                                    logger.error("❌ Saldo not found for card: {}", req.getCardNumber());
                                    return topupCommandRepository.updateTopupStatus(topupId, "FAILED")
                                            .chain(v -> {
                                                throw new ResourceNotFoundException("Saldo not found");
                                            });
                                }

                                int newBalance = saldoResponse.getData().getTotalBalance() + difference;
                                return saldoCommandService.updateSaldoBalance(pb.saldo.SaldoCommand.UpdateSaldoBalanceRequest.newBuilder()
                                                .setCardNumber(saldoResponse.getData().getCardNumber())
                                                .setTotalBalance(newBalance)
                                                .build())
                                        .chain(v -> {
                                            existingTopup.setTopupAmount(req.getTopupAmount().intValue());
                                            existingTopup.setStatus(Status.SUCCESS);
                                            existingTopup.setTopupMethod(req.getTopupMethod());
                                            existingTopup.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                                            return topupCommandRepository.persist(existingTopup);
                                        })
                                        .chain(updatedTopup -> {
                                            logger.info("✅ UpdateTopup completed: card={} topup_id={} new_amount={} new_balance={}",
                                                    req.getCardNumber(), topupId, req.getTopupAmount(), newBalance);

                                            String topupCardCache = "topups:card:" + req.getCardNumber();
                                            String topupIdCache = "topup:id:" + updatedTopup.getTopupId();
                                            String saldoCardCache = "saldo:card:" + req.getCardNumber();
                                            String saldoIdCache = "saldo:id:" + saldoResponse.getData().getSaldoId();

                                            return Uni.combine().all().unis(
                                                    redisService.deleteReactive(topupCardCache),
                                                    redisService.deleteReactive(topupIdCache),
                                                    redisService.deleteReactive(saldoCardCache),
                                                    redisService.deleteReactive(saldoIdCache)
                                            ).asTuple().map(t -> {
                                                span.setStatus(StatusCode.OK);
                                                requestsTotal.add(1, Attributes.of(
                                                        AttributeKey.stringKey("operation"), "update_topup",
                                                        AttributeKey.stringKey("status"), "success"));

                                                return ApiResponse.success("Topup updated successfully for card=" + req.getCardNumber(), TopupResponse.from(updatedTopup));
                                            });
                                        });
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to update topup for card={}", req.getCardNumber(), e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "update_topup",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), null);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "update_topup"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TopupResponseDeleteAt>> trashed(Long topupId) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("trashTopup")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "trash_topup")
                .setAttribute("topupId", String.valueOf(topupId))
                .startSpan();

        logger.info("🗑️ Trashing topup id={}", topupId);

        return topupCommandRepository.trashed(topupId)
                .chain(topup -> {
                    if (topup == null) {
                        logger.error("Topup not found with id {}", topupId);
                        throw new ResourceNotFoundException("Topup not found");
                    }

                    String topupCardCache = "topups:card:" + topup.getCardNumber();
                    String topupIdCache = "topup:id:" + topupId;

                    return Uni.combine().all().unis(
                            redisService.deleteReactive(topupCardCache),
                            redisService.deleteReactive(topupIdCache)
                    ).asTuple().map(t -> {
                        span.setStatus(StatusCode.OK);
                        requestsTotal.add(1, Attributes.of(
                                AttributeKey.stringKey("operation"), "trash_topup",
                                AttributeKey.stringKey("status"), "success"));

                        return ApiResponse.success("Trashed topup id=" + topupId, TopupResponseDeleteAt.from(topup));
                    });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to trash topup id={}", topupId, e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "trash_topup",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), null);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "trash_topup"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<TopupResponseDeleteAt>> restore(Long topupId) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("restoreTopup")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "restore_topup")
                .setAttribute("topupId", String.valueOf(topupId))
                .startSpan();

        logger.info("♻️ Restoring topup id={}", topupId);

        return topupCommandRepository.restore(topupId)
                .chain(topup -> {
                    if (topup == null) {
                        logger.error("Topup not found with id {}", topupId);
                        throw new ResourceNotFoundException("Topup not found");
                    }

                    String topupCardCache = "topups:card:" + topup.getCardNumber();
                    String topupIdCache = "topup:id:" + topupId;

                    return Uni.combine().all().unis(
                            redisService.deleteReactive(topupCardCache),
                            redisService.deleteReactive(topupIdCache)
                    ).asTuple().map(t -> {
                        span.setStatus(StatusCode.OK);
                        requestsTotal.add(1, Attributes.of(
                                AttributeKey.stringKey("operation"), "restore_topup",
                                AttributeKey.stringKey("status"), "success"));

                        return ApiResponse.success("Restored topup id=" + topupId, TopupResponseDeleteAt.from(topup));
                    });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore topup id={}", topupId, e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "restore_topup",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), null);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "restore_topup"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deletePermanent(Long topupId) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("deleteTopupPermanent")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "delete_topup_permanent")
                .setAttribute("topupId", String.valueOf(topupId))
                .startSpan();

        logger.info("🧨 Permanently deleting topup id={}", topupId);

        return topupQueryRepository.findTopupById(topupId)
                .chain(topup -> {
                    if (topup == null) {
                        logger.error("Topup not found with id {}", topupId);
                        throw new ResourceNotFoundException("Topup not found");
                    }

                    String topupCardCache = "topups:card:" + topup.getCardNumber();
                    String topupIdCache = "topup:id:" + topupId;

                    return topupCommandRepository.deletePermanent(topupId)
                            .chain(deleted -> Uni.combine().all().unis(
                                    redisService.deleteReactive(topupCardCache),
                                    redisService.deleteReactive(topupIdCache)
                            ).asTuple().map(t -> {
                                span.setStatus(StatusCode.OK);
                                requestsTotal.add(1, Attributes.of(
                                        AttributeKey.stringKey("operation"), "delete_topup_permanent",
                                        AttributeKey.stringKey("status"), "success"));

                                return ApiResponse.success("Deleted topup id=" + topupId, deleted);
                            }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to permanently delete topup id={}", topupId, e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "delete_topup_permanent",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), false);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "delete_topup_permanent"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> restoreAll() {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("restoreAllTopups")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "restore_all_topups")
                .startSpan();

        logger.info("🔄 Restoring ALL trashed topups");

        return topupCommandRepository.restoreAllDeleted()
                .map(restored -> {
                    span.setStatus(StatusCode.OK);
                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "restore_all_topups",
                            AttributeKey.stringKey("status"), "success"));

                    return ApiResponse.success("Restored all trashed topups", restored);
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to restore all topups", e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "restore_all_topups",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), false);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "restore_all_topups"));
                });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteAll() {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("deleteAllTopups")
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("service.name", "topup-command-service")
                .setAttribute("operation", "delete_all_topups")
                .startSpan();

        logger.info("💣 Permanently deleting ALL trashed topups");

        return topupCommandRepository.deleteAllDeleted()
                .map(deleted -> {
                    span.setStatus(StatusCode.OK);
                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "delete_all_topups",
                            AttributeKey.stringKey("status"), "success"));

                    return ApiResponse.success("Deleted all trashed topups", deleted);
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to delete all topups", e);
                    span.recordException(e);
                    span.setStatus(StatusCode.ERROR, e.getMessage());

                    requestsTotal.add(1, Attributes.of(
                            AttributeKey.stringKey("operation"), "delete_all_topups",
                            AttributeKey.stringKey("status"), "failed",
                            AttributeKey.stringKey("error_type"), e.getClass().getSimpleName()));

                    return new ApiResponse<>("error", e.getMessage(), false);
                })
                .eventually(() -> {
                    span.end();
                    double duration = (System.currentTimeMillis() - startTime) / 1000.0;
                    requestDurationSeconds.record(duration, Attributes.of(
                            AttributeKey.stringKey("operation"), "delete_all_topups"));
                });
    }
}

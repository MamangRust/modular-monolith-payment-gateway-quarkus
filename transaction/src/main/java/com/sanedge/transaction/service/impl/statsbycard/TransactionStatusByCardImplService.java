package com.sanedge.transaction.service.impl.statsbycard;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.transaction.domain.requests.statsbycard.MonthStatusTransactionCardNumber;
import com.sanedge.transaction.domain.requests.statsbycard.YearStatusTransactionCardNumber;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;
import com.sanedge.transaction.repository.statsbycard.TransactionStatusByCardRepository;
import com.sanedge.transaction.service.stats.status.TransactionStatusByCardService;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransactionStatusByCardImplService implements TransactionStatusByCardService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionStatusByCardImplService.class);

    private final TransactionStatusByCardRepository transactionStatusByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransactionStatusByCardImplService(TransactionStatusByCardRepository transactionStatusByCardRepository,
                                              RedisService redisService,
                                              ObjectMapper objectMapper,
                                              TracingMetrics tracingMetrics) {
        this.transactionStatusByCardRepository = transactionStatusByCardRepository;
        this.redisService = redisService;
        this.objectMapper = objectMapper;
        this.tracingMetrics = tracingMetrics;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing object to JSON", e);
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing JSON with TypeReference", e);
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    private boolean isValidYear(Long year) {
        return year != null && year >= 1 && year <= 9999;
    }

    private boolean isValidCard(String cardNumber) {
        return cardNumber != null && !cardNumber.trim().isEmpty();
    }

    private boolean isValidYearMonth(Long year, Integer month) {
        return isValidYear(year) && month != null && month >= 1 && month <= 12;
    }

    @Override
    public Uni<ApiResponse<List<TransactionResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusTransactionCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly SUCCESS transactions for cardNumber={}, year={}, month={}", cardNumber, year, month);

        if (!isValidYearMonth(year, month) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, month={}, cardNumber={}", year, month, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, month, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:status:card:month:success:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .put("month", String.valueOf(month))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthStatusSuccessByCard", "find_month_status_success", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionResponseMonthStatusSuccess> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionResponseMonthStatusSuccess>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Found " + cached.size() + " monthly SUCCESS transaction records", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionStatusByCardRepository.findMonthTransactionStatusSuccessByCard(req)
                                .chain(results -> {
                                    List<TransactionResponseMonthStatusSuccess> response = results.stream()
                                            .map(TransactionResponseMonthStatusSuccess::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Found " + response.size() + " monthly SUCCESS transaction records", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch monthly SUCCESS transactions for cardNumber={}, year={}, month={}", cardNumber, year, month, e);
            return new ApiResponse<>("error", "Failed to fetch monthly SUCCESS transactions", Collections.emptyList());
        });
    }

    @Override
    public Uni<ApiResponse<List<TransactionResponseYearStatusSuccess>>> findYearlyStatusSuccess(YearStatusTransactionCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching yearly SUCCESS transactions for cardNumber={}, year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:status:card:year:success:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearlyStatusSuccessByCard", "find_yearly_status_success", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionResponseYearStatusSuccess>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Found " + cached.size() + " yearly SUCCESS transaction records", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionStatusByCardRepository.findYearlyTransactionStatusSuccessByCard(cardNumber, year)
                                .chain(results -> {
                                    List<TransactionResponseYearStatusSuccess> response = results.stream()
                                            .map(TransactionResponseYearStatusSuccess::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Found " + response.size() + " yearly SUCCESS transaction records", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch yearly SUCCESS transactions for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch yearly SUCCESS transactions", Collections.emptyList());
        });
    }

    @Override
    public Uni<ApiResponse<List<TransactionResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusTransactionCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly FAILED transactions for cardNumber={}, year={}, month={}", cardNumber, year, month);

        if (!isValidYearMonth(year, month) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, month={}, cardNumber={}", year, month, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, month, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:status:card:month:failed:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .put("month", String.valueOf(month))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthStatusFailedByCard", "find_month_status_failed", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionResponseMonthStatusFailed> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionResponseMonthStatusFailed>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Found " + cached.size() + " monthly FAILED transaction records", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionStatusByCardRepository.findMonthTransactionStatusFailedByCard(req)
                                .chain(results -> {
                                    List<TransactionResponseMonthStatusFailed> response = results.stream()
                                            .map(TransactionResponseMonthStatusFailed::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Found " + response.size() + " monthly FAILED transaction records", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch monthly FAILED transactions for cardNumber={}, year={}, month={}", cardNumber, year, month, e);
            return new ApiResponse<>("error", "Failed to fetch monthly FAILED transactions", Collections.emptyList());
        });
    }

    @Override
    public Uni<ApiResponse<List<TransactionResponseYearStatusFailed>>> findYearlyStatusFailed(YearStatusTransactionCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching yearly FAILED transactions for cardNumber={}, year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:status:card:year:failed:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearlyStatusFailedByCard", "find_yearly_status_failed", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionResponseYearStatusFailed> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionResponseYearStatusFailed>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Found " + cached.size() + " yearly FAILED transaction records", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionStatusByCardRepository.findYearlyTransactionStatusFailedByCard(cardNumber, year)
                                .chain(results -> {
                                    List<TransactionResponseYearStatusFailed> response = results.stream()
                                            .map(TransactionResponseYearStatusFailed::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Found " + response.size() + " yearly FAILED transaction records", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch yearly FAILED transactions for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch yearly FAILED transactions", Collections.emptyList());
        });
    }
}
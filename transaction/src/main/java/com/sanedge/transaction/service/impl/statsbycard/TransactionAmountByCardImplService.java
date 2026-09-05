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
import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;
import com.sanedge.transaction.repository.statsbycard.TransactionAmountByCardRepository;
import com.sanedge.transaction.service.stats.amount.TransactionAmountByCardService;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransactionAmountByCardImplService implements TransactionAmountByCardService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionAmountByCardImplService.class);

    private final TransactionAmountByCardRepository transactionAmountByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransactionAmountByCardImplService(TransactionAmountByCardRepository transactionAmountByCardRepository,
                                              RedisService redisService,
                                              ObjectMapper objectMapper,
                                              TracingMetrics tracingMetrics) {
        this.transactionAmountByCardRepository = transactionAmountByCardRepository;
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

    @Override
    public Uni<ApiResponse<List<TransactionMonthAmountResponse>>> findMonthlyAmounts(MonthYearPaymentMethod req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching monthly transaction amounts for cardNumber={}, year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:amount:card:month:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthlyAmountsByCard", "find_monthly_amounts", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionMonthAmountResponse> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionMonthAmountResponse>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Monthly transaction amounts for cardNumber retrieved successfully", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionAmountByCardRepository.findMonthlyAmountsByCard(cardNumber, year)
                                .chain(amounts -> {
                                    List<TransactionMonthAmountResponse> response = amounts.stream()
                                            .map(TransactionMonthAmountResponse::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Monthly transaction amounts for cardNumber", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch monthly transaction amounts for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch monthly transaction amounts", Collections.emptyList());
        });
    }

    @Override
    public Uni<ApiResponse<List<TransactionYearlyAmountResponse>>> findYearlyAmounts(MonthYearPaymentMethod req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching yearly transaction amounts for cardNumber={}, until year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:amount:card:year:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearlyAmountsByCard", "find_yearly_amounts", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionYearlyAmountResponse> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionYearlyAmountResponse>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Yearly transaction amounts for cardNumber retrieved successfully", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionAmountByCardRepository.findYearlyAmountsByCard(cardNumber, year)
                                .chain(amounts -> {
                                    List<TransactionYearlyAmountResponse> response = amounts.stream()
                                            .map(TransactionYearlyAmountResponse::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Yearly transaction amounts for cardNumber", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch yearly transaction amounts for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch yearly transaction amounts", Collections.emptyList());
        });
    }
}
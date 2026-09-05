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
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;
import com.sanedge.transaction.repository.statsbycard.TransactionMethodByCardRepository;
import com.sanedge.transaction.service.stats.method.TransactionMethodByCardService;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransactionMethodByCardImplService implements TransactionMethodByCardService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionMethodByCardImplService.class);

    private final TransactionMethodByCardRepository transactionMethodByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransactionMethodByCardImplService(TransactionMethodByCardRepository transactionMethodByCardRepository,
                                              RedisService redisService,
                                              ObjectMapper objectMapper,
                                              TracingMetrics tracingMetrics) {
        this.transactionMethodByCardRepository = transactionMethodByCardRepository;
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
    public Uni<ApiResponse<List<TransactionMonthMethodResponse>>> findMonthlyMethod(MonthYearPaymentMethod req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching monthly transaction methods for cardNumber={}, year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:method:card:month:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthlyMethodByCard", "find_monthly_method", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionMonthMethodResponse> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionMonthMethodResponse>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Monthly transaction methods retrieved successfully!", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionMethodByCardRepository.findMonthlyPaymentMethodsByCard(cardNumber, year)
                                .chain(methods -> {
                                    List<TransactionMonthMethodResponse> response = methods.stream()
                                            .map(TransactionMonthMethodResponse::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Monthly Transactio methods retrieved successfully!", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch monthly transaction methods for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch monthly transaction methods", Collections.emptyList());
        });
    }

    @Override
    public Uni<ApiResponse<List<TransactionYearMethodResponse>>> findYearlyMethod(MonthYearPaymentMethod req) {
        String cardNumber = req.getCardNumber();
        Long year = req.getYear();

        logger.info("📊 Fetching yearly transaction methods for cardNumber={}, until year={}", cardNumber, year);

        if (!isValidYear(year) || !isValidCard(cardNumber)) {
            logger.error("❌ Invalid input: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year, cardNumber", Collections.emptyList()));
        }

        String cacheKey = String.format("transactions:stats:method:card:year:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearlyMethodByCard", "find_yearly_method", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            List<TransactionYearMethodResponse> cached = fromJson(cachedJson,
                                    new TypeReference<List<TransactionYearMethodResponse>>() {});
                            return Uni.createFrom().item(ApiResponse.success("Yearly transaction methods retrieved successfully!", cached));
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return transactionMethodByCardRepository.findYearlyPaymentMethodsByCard(cardNumber, year)
                                .chain(methods -> {
                                    List<TransactionYearMethodResponse> response = methods.stream()
                                            .map(TransactionYearMethodResponse::from)
                                            .collect(Collectors.toList());

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached response for key: {}", cacheKey);
                                                return ApiResponse.success("Yearly Transactio methods retrieved successfully!", response);
                                            });
                                });
                    });
        }).onFailure().recoverWithItem(e -> {
            logger.error("💥 Failed to fetch yearly transaction methods for cardNumber={}, year={}", cardNumber, year, e);
            return new ApiResponse<>("error", "Failed to fetch yearly transaction methods for cardNumber", Collections.emptyList());
        });
    }
}
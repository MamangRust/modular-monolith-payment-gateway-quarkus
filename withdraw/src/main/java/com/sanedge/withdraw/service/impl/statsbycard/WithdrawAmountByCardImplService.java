package com.sanedge.withdraw.service.impl.statsbycard;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.withdraw.domain.requests.statsbycard.YearMonthCardNumber;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.repository.statsbycard.WithdrawAmountByCardRepository;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountByCardService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WithdrawAmountByCardImplService implements WithdrawAmountByCardService {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawAmountByCardImplService.class);

    private final WithdrawAmountByCardRepository withdrawAmountByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public WithdrawAmountByCardImplService(WithdrawAmountByCardRepository withdrawAmountByCardRepository,
            RedisService redisService,
            ObjectMapper objectMapper,
            TracingMetrics tracingMetrics) {
        this.withdrawAmountByCardRepository = withdrawAmountByCardRepository;
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

    private void validateYear(Long year) {
        if (year == null || year < 1 || year > 9999) {
            throw new IllegalArgumentException("Invalid year");
        }
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new IllegalArgumentException("Card number must not be null or blank");
        }
    }

    @Override
    public Uni<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyByCardNumber(YearMonthCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();

        logger.info("📊 Fetching monthly withdraw amounts for card={} in year={}", cardNumber, year);

        try {
            validateYear(year);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:monthly:amount:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_monthly_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawMonthlyAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawMonthlyAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse
                                .success("Monthly withdraw amounts by card retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthlyWithdrawsByCard", "find_monthly_by_card", attrs,
                            () -> withdrawAmountByCardRepository.findMonthlyWithdrawsByCard(cardNumber, year)
                                    .chain(amounts -> {
                                        List<WithdrawMonthlyAmountResponse> response = amounts.stream()
                                                .map(WithdrawMonthlyAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService
                                                .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success(
                                                        "Monthly withdraw amounts by card retrieved successfully!",
                                                        response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly withdraw amounts by card", e);
                    return new ApiResponse<>("error", "Failed to fetch monthly withdraw amounts by card",
                            Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyByCardNumber(YearMonthCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();

        logger.info("📊 Fetching yearly withdraw amounts for card={} until year={}", cardNumber, year);

        try {
            validateYear(year);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:yearly:amount:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawYearlyAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawYearlyAmountResponse>>() {
                                });
                        return Uni.createFrom().item(
                                ApiResponse.success("Yearly withdraw amounts by card retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyWithdrawsByCard", "find_yearly_by_card", attrs,
                            () -> withdrawAmountByCardRepository.findYearlyWithdrawsByCard(cardNumber, year)
                                    .chain(amounts -> {
                                        List<WithdrawYearlyAmountResponse> response = amounts.stream()
                                                .map(WithdrawYearlyAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService
                                                .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success(
                                                        "Yearly withdraw amounts by card retrieved successfully!",
                                                        response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly withdraw amounts by card", e);
                    return new ApiResponse<>("error", "Failed to fetch yearly withdraw amounts by card",
                            Collections.emptyList());
                });
    }
}

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
import com.sanedge.withdraw.domain.requests.statsbycard.MonthStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.requests.statsbycard.YearStatusWithdrawCardNumber;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.repository.statsbycard.WithdrawStatusByCardRepository;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusByCardService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WithdrawStatusByCardImplService implements WithdrawStatusByCardService {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawStatusByCardImplService.class);

    private final WithdrawStatusByCardRepository withdrawStatusByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public WithdrawStatusByCardImplService(WithdrawStatusByCardRepository withdrawStatusByCardRepository,
                                           RedisService redisService,
                                           ObjectMapper objectMapper,
                                           TracingMetrics tracingMetrics) {
        this.withdrawStatusByCardRepository = withdrawStatusByCardRepository;
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

    private void validateYearMonth(Long year, Integer month) {
        if (year == null || year < 1 || year > 9999)
            throw new IllegalArgumentException("Invalid year");
        if (month == null || month < 1 || month > 12)
            throw new IllegalArgumentException("Invalid month");
    }

    private void validateYear(Long year) {
        if (year == null || year < 1 || year > 9999)
            throw new IllegalArgumentException("Invalid year");
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank())
            throw new IllegalArgumentException("Card number must not be null or blank");
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthStatusSuccessByCard(
            MonthStatusWithdrawCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly SUCCESS withdraw status for card={}, year={}, month={}",
                cardNumber, year, month);

        try {
            validateYearMonth(year, month);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:monthly:success:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_success_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .put("month", month)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseMonthStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseMonthStatusSuccess>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Monthly SUCCESS withdraw status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusSuccessByCard", "find_month_success_by_card", attrs,
                            () -> withdrawStatusByCardRepository.findMonthWithdrawStatusSuccessByCard(req)
                                    .chain(results -> {
                                        List<WithdrawResponseMonthStatusSuccess> response = results.stream()
                                                .map(WithdrawResponseMonthStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly SUCCESS withdraw status by card fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly SUCCESS withdraw status by card", e);
                    return new ApiResponse<>("error", "Failed to fetch monthly SUCCESS withdraw status by card", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(
            YearStatusWithdrawCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();

        logger.info("📊 Fetching yearly SUCCESS withdraw status for card={}, year={}", cardNumber, year);

        try {
            validateYear(year);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:yearly:success:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_success_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseYearStatusSuccess>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Yearly SUCCESS withdraw status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusSuccessByCard", "find_year_success_by_card", attrs,
                            () -> withdrawStatusByCardRepository.findYearlyWithdrawStatusSuccessByCard(cardNumber, year)
                                    .chain(results -> {
                                        List<WithdrawResponseYearStatusSuccess> response = results.stream()
                                                .map(WithdrawResponseYearStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly SUCCESS withdraw status by card fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly SUCCESS withdraw status by card", e);
                    return new ApiResponse<>("error", "Failed to fetch yearly SUCCESS withdraw status by card", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthStatusFailedByCard(
            MonthStatusWithdrawCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly FAILED withdraw status for card={}, year={}, month={}",
                cardNumber, year, month);

        try {
            validateYearMonth(year, month);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:monthly:failed:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_failed_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .put("month", month)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseMonthStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseMonthStatusFailed>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Monthly FAILED withdraw status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusFailedByCard", "find_month_failed_by_card", attrs,
                            () -> withdrawStatusByCardRepository.findMonthWithdrawStatusFailedByCard(req)
                                    .chain(results -> {
                                        List<WithdrawResponseMonthStatusFailed> response = results.stream()
                                                .map(WithdrawResponseMonthStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly FAILED withdraw status by card fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly FAILED withdraw status by card", e);
                    return new ApiResponse<>("error", "Failed to fetch monthly FAILED withdraw status by card", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailedByCard(
            YearStatusWithdrawCardNumber req) {
        String cardNumber = req.getCardNumber();
        long year = req.getYear();

        logger.info("📊 Fetching yearly FAILED withdraw status for card={}, year={}", cardNumber, year);

        try {
            validateYear(year);
            validateCardNumber(cardNumber);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:statsbycard:yearly:failed:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_failed_by_card")
                .put("cardNumber", cardNumber)
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseYearStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseYearStatusFailed>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Yearly FAILED withdraw status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusFailedByCard", "find_year_failed_by_card", attrs,
                            () -> withdrawStatusByCardRepository.findYearlyWithdrawStatusFailedByCard(cardNumber, year)
                                    .chain(results -> {
                                        List<WithdrawResponseYearStatusFailed> response = results.stream()
                                                .map(WithdrawResponseYearStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly FAILED withdraw status by card fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly FAILED withdraw status by card", e);
                    return new ApiResponse<>("error", "Failed to fetch yearly FAILED withdraw status by card", Collections.emptyList());
                });
    }
}

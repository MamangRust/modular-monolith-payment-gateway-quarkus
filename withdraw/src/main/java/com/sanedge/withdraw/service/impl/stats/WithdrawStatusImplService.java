package com.sanedge.withdraw.service.impl.stats;

import java.time.LocalDate;
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
import com.sanedge.withdraw.domain.requests.MonthStatusWithdraw;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.repository.stats.WithdrawStatusRepository;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WithdrawStatusImplService implements WithdrawStatusService {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawStatusImplService.class);

    private final WithdrawStatusRepository withdrawStatusRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public WithdrawStatusImplService(WithdrawStatusRepository withdrawStatusRepository,
                                     RedisService redisService,
                                     ObjectMapper objectMapper,
                                     TracingMetrics tracingMetrics) {
        this.withdrawStatusRepository = withdrawStatusRepository;
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

    @Override
    public Uni<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusWithdraw req) {
        Long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly SUCCESS withdraw status for year: {}, month: {}", year, month);

        try {
            validateYearMonth(year, month);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:monthly:success:%d:%d", year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_success")
                .put("year", year)
                .put("month", month)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseMonthStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseMonthStatusSuccess>>() {
                                });
                        return Uni.createFrom().item(
                                ApiResponse.success("Monthly SUCCESS withdraw status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusSuccess", "find_month_success", attrs,
                            () -> {
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return withdrawStatusRepository.findMonthWithdrawStatusSuccess(
                                                year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(results -> {
                                            List<WithdrawResponseMonthStatusSuccess> response = results.stream()
                                                    .map(WithdrawResponseMonthStatusSuccess::from)
                                                    .collect(Collectors.toList());

                                            return redisService
                                                    .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Monthly SUCCESS withdraw status fetched successfully", response));
                                        });
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly SUCCESS withdraw status", e);
                    return new ApiResponse<>("error", "Failed to fetch monthly SUCCESS withdraw status", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year) {
        logger.info("📊 Fetching yearly SUCCESS withdraw status for year: {}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:yearly:success:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_success")
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseYearStatusSuccess>>() {
                                });
                        return Uni.createFrom().item(
                                ApiResponse.success("Yearly SUCCESS withdraw status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusSuccess", "find_year_success", attrs,
                            () -> withdrawStatusRepository.findYearlyWithdrawStatusSuccess(year)
                                    .chain(results -> {
                                        List<WithdrawResponseYearStatusSuccess> response = results.stream()
                                                .map(WithdrawResponseYearStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService
                                                .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success(
                                                        "Yearly SUCCESS withdraw status fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly SUCCESS withdraw status", e);
                    return new ApiResponse<>("error", "Failed to fetch yearly SUCCESS withdraw status", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusWithdraw req) {
        Long year = req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly FAILED withdraw status for year: {}, month: {}", year, month);

        try {
            validateYearMonth(year, month);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:monthly:failed:%d:%d", year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_failed")
                .put("year", year)
                .put("month", month)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseMonthStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseMonthStatusFailed>>() {
                                });
                        return Uni.createFrom().item(
                                ApiResponse.success("Monthly FAILED withdraw status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusFailed", "find_month_failed", attrs,
                            () -> {
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return withdrawStatusRepository.findMonthWithdrawStatusFailed(
                                                year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(results -> {
                                            List<WithdrawResponseMonthStatusFailed> response = results.stream()
                                                    .map(WithdrawResponseMonthStatusFailed::from)
                                                    .collect(Collectors.toList());

                                            return redisService
                                                    .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Monthly FAILED withdraw status fetched successfully", response));
                                        });
                            });
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly FAILED withdraw status", e);
                    return new ApiResponse<>("error", "Failed to fetch monthly FAILED withdraw status", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailed(Long year) {
        logger.info("📊 Fetching yearly FAILED withdraw status for year: {}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input: {}", e.getMessage());
            return Uni.createFrom().item(new ApiResponse<>("error", "invalid input", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:yearly:failed:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_failed")
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawResponseYearStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawResponseYearStatusFailed>>() {
                                });
                        return Uni.createFrom().item(
                                ApiResponse.success("Yearly FAILED withdraw status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusFailed", "find_year_failed", attrs,
                            () -> withdrawStatusRepository.findYearlyWithdrawStatusFailed(year)
                                    .chain(results -> {
                                        List<WithdrawResponseYearStatusFailed> response = results.stream()
                                                .map(WithdrawResponseYearStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService
                                                .setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success(
                                                        "Yearly FAILED withdraw status fetched successfully", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly FAILED withdraw status", e);
                    return new ApiResponse<>("error", "Failed to fetch yearly FAILED withdraw status", Collections.emptyList());
                });
    }
}

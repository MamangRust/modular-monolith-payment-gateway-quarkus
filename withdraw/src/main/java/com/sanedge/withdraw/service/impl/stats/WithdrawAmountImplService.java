package com.sanedge.withdraw.service.impl.stats;

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
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.repository.stats.WithdrawAmountRepository;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WithdrawAmountImplService implements WithdrawAmountService {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawAmountImplService.class);

    private final WithdrawAmountRepository withdrawAmountRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public WithdrawAmountImplService(WithdrawAmountRepository withdrawAmountRepository,
                                     RedisService redisService,
                                     ObjectMapper objectMapper,
                                     TracingMetrics tracingMetrics) {
        this.withdrawAmountRepository = withdrawAmountRepository;
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

    @Override
    public Uni<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyWithdraws(Long year) {
        logger.info("📊 Fetching monthly withdraw amounts for year={}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input year={}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year provided", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:monthly:amount:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_monthly_amount")
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawMonthlyAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawMonthlyAmountResponse>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Monthly withdraw amounts retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthlyWithdrawsAmount", "find_monthly_amount", attrs,
                            () -> withdrawAmountRepository.findMonthlyWithdraws(year)
                                    .chain(amounts -> {
                                        List<WithdrawMonthlyAmountResponse> response = amounts.stream()
                                                .map(WithdrawMonthlyAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly withdraw amounts retrieved successfully!", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly withdraw amounts for year={}", year, e);
                    return new ApiResponse<>("error", "Failed to retrieve monthly withdraw amounts", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyWithdraws(Long year) {
        logger.info("📊 Fetching yearly withdraw amounts until year={}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Invalid input year={}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year provided", Collections.emptyList()));
        }

        String cacheKey = String.format("withdraws:stats:yearly:amount:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_amount")
                .put("year", year)
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<WithdrawYearlyAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<WithdrawYearlyAmountResponse>>() {});
                        return Uni.createFrom().item(ApiResponse.success("Yearly withdraw amounts retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyWithdrawsAmount", "find_yearly_amount", attrs,
                            () -> withdrawAmountRepository.findYearlyWithdraws(year)
                                    .chain(amounts -> {
                                        List<WithdrawYearlyAmountResponse> response = amounts.stream()
                                                .map(WithdrawYearlyAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly withdraw amounts retrieved successfully!", response));
                                    }));
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly withdraw amounts until year={}", year, e);
                    return new ApiResponse<>("error", "Failed to retrieve yearly withdraw amounts", Collections.emptyList());
                });
    }
}

package com.sanedge.transfer.service.impl.stats;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.repository.stats.TransferAmountRepository;
import com.sanedge.transfer.service.stats.amount.TransferAmountService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransferAmountImplService implements TransferAmountService {
    private static final Logger logger = LoggerFactory.getLogger(TransferAmountImplService.class);

    private final TransferAmountRepository transferAmountRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransferAmountImplService(TransferAmountRepository transferAmountRepository,
                                     RedisService redisService,
                                     ObjectMapper objectMapper,
                                     TracingMetrics tracingMetrics) {
        this.transferAmountRepository = transferAmountRepository;
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

    @Override
    public Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmounts(Long year) {
        logger.info("📊 Fetching monthly transfer amounts for year={}", year);

        if (!isValidYear(year)) {
            logger.error("❌ Invalid input year: {}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:amount:month:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_monthly_amounts")
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferMonthAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferMonthAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Monthly transfer amounts retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthlyTransferAmounts", "find_monthly_amounts", attrs,
                            () -> transferAmountRepository.findMonthlyTransferAmounts(year)
                                    .chain(amounts -> {
                                        List<TransferMonthAmountResponse> response = amounts.stream()
                                                .map(TransferMonthAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly transfer amounts retrieved successfully!", response));
                                    })
                                );
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch monthly transfer amounts for year={}", year, e);
                    return new ApiResponse<>("error", "Failed to retrieve monthly transfer amounts", Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmounts(Long year) {
        logger.info("📊 Fetching yearly transfer amounts until year={}", year);

        if (!isValidYear(year)) {
            logger.error("❌ Invalid input year: {}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input - year", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:amount:year:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_amounts")
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferYearAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferYearAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly transfer amounts retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyTransferAmounts", "find_yearly_amounts", attrs,
                            () -> transferAmountRepository.findYearlyTransferAmounts(year)
                                    .chain(amounts -> {
                                        List<TransferYearAmountResponse> response = amounts.stream()
                                                .map(TransferYearAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly transfer amounts retrieved successfully!", response));
                                    })
                                );
                })
                .onFailure().recoverWithItem(e -> {
                    logger.error("❌ Failed to fetch yearly transfer amounts until year={}", year, e);
                    return new ApiResponse<>("error", "Failed to retrieve yearly transfer amounts", Collections.emptyList());
                });
    }
}

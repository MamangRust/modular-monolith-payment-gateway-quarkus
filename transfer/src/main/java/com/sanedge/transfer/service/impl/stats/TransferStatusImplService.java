package com.sanedge.transfer.service.impl.stats;

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
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.transfer.domain.requests.stats.MonthStatusTransfer;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.transfer.repository.stats.TransferStatusRepository;
import com.sanedge.transfer.service.stats.status.TransferStatusService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransferStatusImplService implements TransferStatusService {
    private static final Logger logger = LoggerFactory.getLogger(TransferStatusImplService.class);

    private final TransferStatusRepository transferStatusRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransferStatusImplService(TransferStatusRepository transferStatusRepository,
                                     RedisService redisService,
                                     ObjectMapper objectMapper,
                                     TracingMetrics tracingMetrics) {
        this.transferStatusRepository = transferStatusRepository;
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

    private boolean isValidYearMonth(Long year, Integer month) {
        return isValidYear(year) && month != null && month >= 1 && month <= 12;
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusTransfer req) {
        Long year = (long) req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly SUCCESS transfer status for year: {}, month: {}", year, month);

        if (!isValidYearMonth(year, month)) {
            logger.error("❌ Invalid year/month: year={}, month={}", year, month);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year or month", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:status:month:success:%d:%d", year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_status_success")
                .put("year", String.valueOf(year))
                .put("month", String.valueOf(month))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseMonthStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseMonthStatusSuccess>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Monthly SUCCESS transfer status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthTransferStatusSuccess", "find_month_status_success", attrs,
                            () -> {
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return transferStatusRepository.findMonthTransferStatusSuccess(year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(results -> {
                                            List<TransferResponseMonthStatusSuccess> response = results.stream()
                                                    .map(TransferResponseMonthStatusSuccess::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success("Monthly SUCCESS transfer status fetched successfully", response));
                                        })
                                        .onFailure().recoverWithItem(e -> {
                                            logger.error("❌ Failed to fetch monthly SUCCESS transfer status for year={}, month={}", year, month, e);
                                            return new ApiResponse<>("error", "Failed to retrieve monthly SUCCESS transfer status", Collections.emptyList());
                                        });
                            });
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusTransfer req) {
        Long year = (long) req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly FAILED transfer status for year: {}, month: {}", year, month);

        if (!isValidYearMonth(year, month)) {
            logger.error("❌ Invalid year/month: year={}, month={}", year, month);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year or month", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:status:month:failed:%d:%d", year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_status_failed")
                .put("year", String.valueOf(year))
                .put("month", String.valueOf(month))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseMonthStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseMonthStatusFailed>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Monthly FAILED transfer status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthTransferStatusFailed", "find_month_status_failed", attrs,
                            () -> {
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return transferStatusRepository.findMonthTransferStatusFailed(year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(results -> {
                                            List<TransferResponseMonthStatusFailed> response = results.stream()
                                                    .map(TransferResponseMonthStatusFailed::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success("Monthly FAILED transfer status fetched successfully", response));
                                        })
                                        .onFailure().recoverWithItem(e -> {
                                            logger.error("❌ Failed to fetch monthly FAILED transfer status for year={}, month={}", year, month, e);
                                            return new ApiResponse<>("error", "Failed to retrieve monthly FAILED transfer status", Collections.emptyList());
                                        });
                            });
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year) {
        logger.info("📊 Fetching yearly SUCCESS transfer status for year: {}", year);

        if (!isValidYear(year)) {
            logger.error("❌ Invalid year: {}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:status:year:success:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_status_success")
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseYearStatusSuccess>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly SUCCESS transfer status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyTransferStatusSuccess", "find_yearly_status_success", attrs,
                            () -> transferStatusRepository.findYearlyTransferStatusSuccess(year)
                                    .chain(results -> {
                                        List<TransferResponseYearStatusSuccess> response = results.stream()
                                                .map(TransferResponseYearStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly SUCCESS transfer status fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly SUCCESS transfer status for year={}", year, e);
                                        return new ApiResponse<>("error", "Failed to retrieve yearly SUCCESS transfer status", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailed(Long year) {
        logger.info("📊 Fetching yearly FAILED transfer status for year: {}", year);

        if (!isValidYear(year)) {
            logger.error("❌ Invalid year: {}", year);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid year", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:stats:status:year:failed:%d", year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_status_failed")
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseYearStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseYearStatusFailed>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly FAILED transfer status fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyTransferStatusFailed", "find_yearly_status_failed", attrs,
                            () -> transferStatusRepository.findYearlyTransferStatusFailed(year)
                                    .chain(results -> {
                                        List<TransferResponseYearStatusFailed> response = results.stream()
                                                .map(TransferResponseYearStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly FAILED transfer status fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly FAILED transfer status for year={}", year, e);
                                        return new ApiResponse<>("error", "Failed to retrieve yearly FAILED transfer status", Collections.emptyList());
                                    }));
                });
    }
}

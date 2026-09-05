package com.sanedge.topup.service.impl.stats;

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
import com.sanedge.topup.domain.requests.stats.MonthTopupStatus;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.topup.repository.stats.TopupStatusRepository;
import com.sanedge.topup.service.stats.status.TopupStatusService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TopupStatusServiceImpl implements TopupStatusService {
    private static final Logger logger = LoggerFactory.getLogger(TopupStatusServiceImpl.class);

    private final TopupStatusRepository topupStatusRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TopupStatusServiceImpl(TopupStatusRepository topupStatusRepository,
                                 RedisService redisService,
                                 ObjectMapper objectMapper,
                                 TracingMetrics tracingMetrics) {
        this.topupStatusRepository = topupStatusRepository;
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
        if (year == null || year < 1 || year > 9999) {
            logger.error("❌ Invalid year: {}", year);
            throw new IllegalArgumentException("Invalid year");
        }
        if (month == null || month < 1 || month > 12) {
            logger.error("❌ Invalid month: {}", month);
            throw new IllegalArgumentException("Invalid month");
        }
    }

    private void validateYear(Long year) {
        if (year == null || year < 1 || year > 9999) {
            logger.error("❌ Invalid year: {}", year);
            throw new IllegalArgumentException("Invalid year");
        }
    }

    @Override
    public Uni<ApiResponse<List<TopupResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthTopupStatus req) {
        Long year = req.getYear();
        int month = req.getMonth();
        logger.info("📊 Fetching monthly SUCCESS topup status for year={} month={}", year, month);

        try {
            validateYearMonth(year, month);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = String.format("topups:stats:month:success:%d:%d", year, month);

        return tracingMetrics.traceAndMeasure("findMonthStatusSuccess", "find_month_status_success",
                Attributes.builder()
                        .put("service.name", "topup-status-service")
                        .put("operation", "find_month_status_success")
                        .put("year", String.valueOf(year))
                        .put("month", String.valueOf(month))
                        .build(), () -> {
                    return redisService.getReactive(cacheKey)
                            .chain(cachedJson -> {
                                if (cachedJson != null) {
                                    logger.info("Cache HIT for key: {}", cacheKey);
                                    List<TopupResponseMonthStatusSuccess> cached = fromJson(cachedJson,
                                            new TypeReference<List<TopupResponseMonthStatusSuccess>>() {
                                            });
                                    return Uni.createFrom()
                                            .item(ApiResponse.success("Successfully fetched monthly SUCCESS topup status", cached));
                                }

                                logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return topupStatusRepository
                                        .findMonthTopupStatusSuccess(year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(data -> {
                                            List<TopupResponseMonthStatusSuccess> responseList = data.stream()
                                                    .map(TopupResponseMonthStatusSuccess::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(responseList),
                                                    STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Successfully fetched monthly SUCCESS topup status", responseList));
                                        });
                            });
                }).onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to fetch monthly SUCCESS topup status month={} year={}", month, year, e);
                    return new ApiResponse<>("error", "Failed to fetch monthly SUCCESS topup status",
                            Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<TopupResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year) {
        logger.info("📊 Fetching yearly SUCCESS topup status for year={}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = String.format("topups:stats:year:success:%d", year);

        return tracingMetrics.traceAndMeasure("findYearlyStatusSuccess", "find_yearly_status_success",
                Attributes.builder()
                        .put("service.name", "topup-status-service")
                        .put("operation", "find_yearly_status_success")
                        .put("year", String.valueOf(year))
                        .build(), () -> {
                    return redisService.getReactive(cacheKey)
                            .chain(cachedJson -> {
                                if (cachedJson != null) {
                                    logger.info("Cache HIT for key: {}", cacheKey);
                                    List<TopupResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                            new TypeReference<List<TopupResponseYearStatusSuccess>>() {
                                            });
                                    return Uni.createFrom()
                                            .item(ApiResponse.success("Successfully fetched yearly SUCCESS topup status", cached));
                                }

                                logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                                return topupStatusRepository.findYearlyTopupStatusSuccess(year)
                                        .chain(data -> {
                                            List<TopupResponseYearStatusSuccess> responseList = data.stream()
                                                    .map(TopupResponseYearStatusSuccess::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(responseList),
                                                    STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Successfully fetched yearly SUCCESS topup status", responseList));
                                        });
                            });
                }).onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to fetch yearly SUCCESS topup status year={}", year, e);
                    return new ApiResponse<>("error", "Failed to fetch yearly SUCCESS topup status",
                            Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<TopupResponseMonthStatusFailed>>> findMonthStatusFailed(MonthTopupStatus req) {
        Long year = req.getYear();
        int month = req.getMonth();
        logger.info("📊 Fetching monthly FAILED topup status for year={} month={}", year, month);

        try {
            validateYearMonth(year, month);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = String.format("topups:stats:month:failed:%d:%d", year, month);

        return tracingMetrics.traceAndMeasure("findMonthStatusFailed", "find_month_status_failed",
                Attributes.builder()
                        .put("service.name", "topup-status-service")
                        .put("operation", "find_month_status_failed")
                        .put("year", String.valueOf(year))
                        .put("month", String.valueOf(month))
                        .build(), () -> {
                    return redisService.getReactive(cacheKey)
                            .chain(cachedJson -> {
                                if (cachedJson != null) {
                                    logger.info("Cache HIT for key: {}", cacheKey);
                                    List<TopupResponseMonthStatusFailed> cached = fromJson(cachedJson,
                                            new TypeReference<List<TopupResponseMonthStatusFailed>>() {
                                            });
                                    return Uni.createFrom()
                                            .item(ApiResponse.success("Successfully fetched monthly FAILED topup status", cached));
                                }

                                logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                                LocalDate currentMonth = LocalDate.of(year.intValue(), month, 1);
                                LocalDate nextMonth = currentMonth.plusMonths(1);

                                return topupStatusRepository
                                        .findMonthTopupStatusFailed(year, month, (long) nextMonth.getYear(), nextMonth.getMonthValue())
                                        .chain(data -> {
                                            List<TopupResponseMonthStatusFailed> responseList = data.stream()
                                                    .map(TopupResponseMonthStatusFailed::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(responseList),
                                                    STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Successfully fetched monthly FAILED topup status", responseList));
                                        });
                            });
                }).onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to fetch monthly FAILED topup status month={} year={}", month, year, e);
                    return new ApiResponse<>("error", "Failed to fetch monthly FAILED topup status",
                            Collections.emptyList());
                });
    }

    @Override
    public Uni<ApiResponse<List<TopupResponseYearStatusFailed>>> findYearlyStatusFailed(Long year) {
        logger.info("📊 Fetching yearly FAILED topup status for year={}", year);

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = String.format("topups:stats:year:failed:%d", year);

        return tracingMetrics.traceAndMeasure("findYearlyStatusFailed", "find_yearly_status_failed",
                Attributes.builder()
                        .put("service.name", "topup-status-service")
                        .put("operation", "find_yearly_status_failed")
                        .put("year", String.valueOf(year))
                        .build(), () -> {
                    return redisService.getReactive(cacheKey)
                            .chain(cachedJson -> {
                                if (cachedJson != null) {
                                    logger.info("Cache HIT for key: {}", cacheKey);
                                    List<TopupResponseYearStatusFailed> cached = fromJson(cachedJson,
                                            new TypeReference<List<TopupResponseYearStatusFailed>>() {
                                            });
                                    return Uni.createFrom()
                                            .item(ApiResponse.success("Successfully fetched yearly FAILED topup status", cached));
                                }

                                logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                                return topupStatusRepository.findYearlyTopupStatusFailed(year)
                                        .chain(data -> {
                                            List<TopupResponseYearStatusFailed> responseList = data.stream()
                                                    .map(TopupResponseYearStatusFailed::from)
                                                    .collect(Collectors.toList());

                                            return redisService.setWithExpirationReactive(cacheKey, toJson(responseList),
                                                    STATS_CACHE_TTL_SECONDS)
                                                    .map(v -> ApiResponse.success(
                                                            "Successfully fetched yearly FAILED topup status", responseList));
                                        });
                            });
                }).onFailure().recoverWithItem(e -> {
                    logger.error("💥 Failed to fetch yearly FAILED topup status year={}", year, e);
                    return new ApiResponse<>("error", "Failed to fetch yearly FAILED topup status",
                            Collections.emptyList());
                });
    }
}

package com.sanedge.merchant.service.impl.stats;

import java.time.LocalDate;
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
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;
import com.sanedge.merchant.repository.stats.MerchantTotalAmountRepository;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantTotalAmountServiceImpl implements MerchantTotalAmountService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantTotalAmountServiceImpl.class);

    private final MerchantTotalAmountRepository merchantTotalAmountRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantTotalAmountServiceImpl(MerchantTotalAmountRepository merchantTotalAmountRepository,
                                         RedisService redisService,
                                         ObjectMapper objectMapper,
                                         TracingMetrics tracingMetrics) {
        this.merchantTotalAmountRepository = merchantTotalAmountRepository;
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
            logger.error("❌ Invalid year: {}", year);
            throw new IllegalArgumentException("Invalid year");
        }
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> findMonthTotalAmount(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = "merchant-stats:total-amount:monthly:" + year;

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_total_amount")
                .put("service.name", "merchant-total-amount-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthTotalAmount", "find_month_total_amount", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseMonthlyTotalAmount>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        LocalDate yearDate = LocalDate.of(year.intValue(), 1, 1);

                        return merchantTotalAmountRepository.findMonthlyTotalAmount(yearDate)
                                .chain(monthlyTotals -> {
                                    List<MerchantResponseMonthlyTotalAmount> responseList = monthlyTotals.stream()
                                            .map(MerchantResponseMonthlyTotalAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseMonthlyTotalAmount>> response = ApiResponse.success(
                                            "Successfully fetched monthly total merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached monthly total merchant amounts for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch monthly total merchant amounts for year={}", year, e);
        });
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> findYearTotalAmount(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = "merchant-stats:total-amount:yearly:" + year;

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_total_amount")
                .put("service.name", "merchant-total-amount-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearTotalAmount", "find_year_total_amount", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseYearlyTotalAmount>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseYearlyTotalAmount>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        Long prevYear = year - 1L;

                        return merchantTotalAmountRepository.findYearlyTotalAmount(year, prevYear)
                                .chain(yearlyTotals -> {
                                    List<MerchantResponseYearlyTotalAmount> responseList = yearlyTotals.stream()
                                            .map(MerchantResponseYearlyTotalAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseYearlyTotalAmount>> response = ApiResponse.success(
                                            "Successfully fetched yearly total merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached yearly total merchant amounts for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch yearly total merchant amounts for year={}", year, e);
        });
    }
}
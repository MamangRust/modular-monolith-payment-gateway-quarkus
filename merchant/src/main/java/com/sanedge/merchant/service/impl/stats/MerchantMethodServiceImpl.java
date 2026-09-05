package com.sanedge.merchant.service.impl.stats;

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
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;
import com.sanedge.merchant.repository.stats.MerchantMethodRepository;
import com.sanedge.merchant.service.stats.method.MerchantMethodService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantMethodServiceImpl implements MerchantMethodService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantMethodServiceImpl.class);

    private final MerchantMethodRepository merchantMethodRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantMethodServiceImpl(MerchantMethodRepository merchantMethodRepository,
                                     RedisService redisService,
                                     ObjectMapper objectMapper,
                                     TracingMetrics tracingMetrics) {
        this.merchantMethodRepository = merchantMethodRepository;
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
    public Uni<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> findMonthMethod(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = "merchant-stats:method:monthly:" + year;

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_method")
                .put("service.name", "merchant-method-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthMethod", "find_month_method", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantMethodRepository.findMonthlyPaymentMethods(year)
                                .chain(monthlyMethods -> {
                                    List<MerchantResponseMonthlyPaymentMethod> responseList = monthlyMethods.stream()
                                            .map(MerchantResponseMonthlyPaymentMethod::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> response = ApiResponse.success(
                                            "Successfully fetched monthly payment method stats for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached monthly payment method stats for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch monthly payment method stats for year={}", year, e);
        });
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> findYearMethod(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = "merchant-stats:method:yearly:" + year;

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_method")
                .put("service.name", "merchant-method-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearMethod", "find_year_method", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseYearlyPaymentMethod>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantMethodRepository.findYearlyPaymentMethods(year)
                                .chain(yearlyMethods -> {
                                    List<MerchantResponseYearlyPaymentMethod> responseList = yearlyMethods.stream()
                                            .map(MerchantResponseYearlyPaymentMethod::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseYearlyPaymentMethod>> response = ApiResponse.success(
                                            "Successfully fetched yearly payment method stats for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached yearly payment method stats for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch yearly payment method stats for year={}", year, e);
        });
    }
}
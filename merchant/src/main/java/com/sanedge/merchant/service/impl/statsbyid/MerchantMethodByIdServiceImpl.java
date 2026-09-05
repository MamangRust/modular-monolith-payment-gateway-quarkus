package com.sanedge.merchant.service.impl.statsbyid;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearPaymentMethodMerchant;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;
import com.sanedge.merchant.repository.statsbyid.MerchantMethodByIdRepository;
import com.sanedge.merchant.service.stats.method.MerchantMethodByIdService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantMethodByIdServiceImpl implements MerchantMethodByIdService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantMethodByIdServiceImpl.class);

    private final MerchantMethodByIdRepository merchantMethodByIdRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantMethodByIdServiceImpl(MerchantMethodByIdRepository merchantMethodByIdRepository,
                                         RedisService redisService,
                                         ObjectMapper objectMapper,
                                         TracingMetrics tracingMetrics) {
        this.merchantMethodByIdRepository = merchantMethodByIdRepository;
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
    public Uni<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> findMonthMethodById(MonthYearPaymentMethodMerchant req) {
        Long merchantId = req.getMerchantId();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-id:method:monthly:%d:%d", merchantId, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_method_by_id")
                .put("service.name", "merchant-method-by-id-service")
                .put("merchantId", String.valueOf(merchantId))
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthMethodById", "find_month_method_by_id", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantMethodByIdRepository.findMonthlyPaymentMethodsById(merchantId, year)
                                .chain(monthlyMethods -> {
                                    List<MerchantResponseMonthlyPaymentMethod> responseList = monthlyMethods.stream()
                                            .map(MerchantResponseMonthlyPaymentMethod::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> response = ApiResponse.success(
                                            "Successfully fetched monthly payment method stats for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached monthly payment method stats by ID for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch monthly payment method stats for merchantId={} year={}", merchantId, year, e);
        });
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> findYearMethodById(MonthYearPaymentMethodMerchant req) {
        Long merchantId = req.getMerchantId();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-id:method:yearly:%d:%d", merchantId, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_method_by_id")
                .put("service.name", "merchant-method-by-id-service")
                .put("merchantId", String.valueOf(merchantId))
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearMethodById", "find_year_method_by_id", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseYearlyPaymentMethod>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantMethodByIdRepository.findYearlyPaymentMethodsById(merchantId, year)
                                .chain(yearlyMethods -> {
                                    List<MerchantResponseYearlyPaymentMethod> responseList = yearlyMethods.stream()
                                            .map(MerchantResponseYearlyPaymentMethod::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseYearlyPaymentMethod>> response = ApiResponse.success(
                                            "Successfully fetched yearly payment method stats for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached yearly payment method stats by ID for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch yearly payment method stats for merchantId={} year={}", merchantId, year, e);
        });
    }
}
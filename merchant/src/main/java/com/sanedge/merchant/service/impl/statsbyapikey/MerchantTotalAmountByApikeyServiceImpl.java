package com.sanedge.merchant.service.impl.statsbyapikey;

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
import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearTotalAmountApiKey;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;
import com.sanedge.merchant.repository.statsbyapikey.MerchantTotalAmountByApiKeyRepository;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountByApiKeyService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantTotalAmountByApikeyServiceImpl implements MerchantTotalAmountByApiKeyService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantTotalAmountByApikeyServiceImpl.class);

    private final MerchantTotalAmountByApiKeyRepository merchantTotalAmountByApiKeyRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantTotalAmountByApikeyServiceImpl(MerchantTotalAmountByApiKeyRepository merchantTotalAmountByApiKeyRepository,
                                                 RedisService redisService,
                                                 ObjectMapper objectMapper,
                                                 TracingMetrics tracingMetrics) {
        this.merchantTotalAmountByApiKeyRepository = merchantTotalAmountByApiKeyRepository;
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
    public Uni<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> findMonthTotalAmountByApiKey(MonthYearTotalAmountApiKey req) {
        String apiKey = req.getApiKey();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-apikey:total-amount:monthly:%s:%d", apiKey, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_total_amount_by_apikey")
                .put("service.name", "merchant-total-amount-by-apikey-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthTotalAmountByApiKey", "find_month_total_amount_by_apikey", attrs, () -> {
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

                        return merchantTotalAmountByApiKeyRepository.findMonthlyTotalAmountByApiKey(apiKey, yearDate)
                                .chain(monthlyTotals -> {
                                    List<MerchantResponseMonthlyTotalAmount> responseList = monthlyTotals.stream()
                                            .map(MerchantResponseMonthlyTotalAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseMonthlyTotalAmount>> response = ApiResponse.success(
                                            "Successfully fetched monthly total merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached monthly total merchant amounts by apiKey for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch monthly total merchant amounts for year={} apiKey={}", year, apiKey, e);
        });
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> findYearTotalAmountByApiKey(MonthYearTotalAmountApiKey req) {
        String apiKey = req.getApiKey();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-apikey:total-amount:yearly:%s:%d", apiKey, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_total_amount_by_apikey")
                .put("service.name", "merchant-total-amount-by-apikey-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearTotalAmountByApiKey", "find_year_total_amount_by_apikey", attrs, () -> {
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

                        return merchantTotalAmountByApiKeyRepository.findYearlyTotalAmountByApiKey(apiKey, year, prevYear)
                                .chain(yearlyTotals -> {
                                    List<MerchantResponseYearlyTotalAmount> responseList = yearlyTotals.stream()
                                            .map(MerchantResponseYearlyTotalAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseYearlyTotalAmount>> response = ApiResponse.success(
                                            "Successfully fetched yearly total merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached yearly total merchant amounts by apiKey for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch yearly total merchant amounts for year={} apiKey={}", year, apiKey, e);
        });
    }
}
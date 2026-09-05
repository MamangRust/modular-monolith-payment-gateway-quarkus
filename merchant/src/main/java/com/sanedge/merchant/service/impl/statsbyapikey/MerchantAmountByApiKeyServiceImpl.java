package com.sanedge.merchant.service.impl.statsbyapikey;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearAmountApiKey;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.merchant.repository.statsbyapikey.MerchantAmountByApiKeyRepository;
import com.sanedge.merchant.service.stats.amount.MerchantAmountByApiKeyService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantAmountByApiKeyServiceImpl implements MerchantAmountByApiKeyService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantAmountByApiKeyServiceImpl.class);

    private final MerchantAmountByApiKeyRepository merchantAmountByApiKeyRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantAmountByApiKeyServiceImpl(MerchantAmountByApiKeyRepository merchantAmountByApiKeyRepository,
                                             RedisService redisService,
                                             ObjectMapper objectMapper,
                                             TracingMetrics tracingMetrics) {
        this.merchantAmountByApiKeyRepository = merchantAmountByApiKeyRepository;
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
    public Uni<ApiResponse<List<MerchantResponseMonthlyAmount>>> findMonthAmountByApiKey(MonthYearAmountApiKey req) {
        String apiKey = req.getApiKey();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-apikey:amount:monthly:%s:%d", apiKey, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_amount_by_apikey")
                .put("service.name", "merchant-amount-by-apikey-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findMonthAmountByApiKey", "find_month_amount_by_apikey", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseMonthlyAmount>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseMonthlyAmount>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantAmountByApiKeyRepository.findMonthlyAmountByApiKey(apiKey, year)
                                .chain(monthlyAmounts -> {
                                    List<MerchantResponseMonthlyAmount> responseList = monthlyAmounts.stream()
                                            .map(MerchantResponseMonthlyAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseMonthlyAmount>> response = ApiResponse.success(
                                            "Successfully fetched monthly merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached monthly merchant amounts by apiKey for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch monthly merchant amounts for year={} apiKey={}", year, apiKey, e);
        });
    }

    @Override
    public Uni<ApiResponse<List<MerchantResponseYearlyAmount>>> findYearAmountByApiKey(MonthYearAmountApiKey req) {
        String apiKey = req.getApiKey();
        Long year = (long) req.getYear();

        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), java.util.Collections.emptyList()));
        }

        String cacheKey = String.format("merchant-stats-apikey:amount:yearly:%s:%d", apiKey, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_year_amount_by_apikey")
                .put("service.name", "merchant-amount-by-apikey-service")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.traceAndMeasure("findYearAmountByApiKey", "find_year_amount_by_apikey", attrs, () -> {
            return redisService.getReactive(cacheKey)
                    .chain(cachedJson -> {
                        if (cachedJson != null) {
                            logger.info("Cache HIT for key: {}", cacheKey);
                            ApiResponse<List<MerchantResponseYearlyAmount>> response = fromJson(cachedJson,
                                    new TypeReference<ApiResponse<List<MerchantResponseYearlyAmount>>>() {});
                            return Uni.createFrom().item(response);
                        }

                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                        return merchantAmountByApiKeyRepository.findYearlyAmountByApiKey(apiKey, year)
                                .chain(yearlyAmounts -> {
                                    List<MerchantResponseYearlyAmount> responseList = yearlyAmounts.stream()
                                            .map(MerchantResponseYearlyAmount::from)
                                            .collect(Collectors.toList());

                                    ApiResponse<List<MerchantResponseYearlyAmount>> response = ApiResponse.success(
                                            "Successfully fetched yearly merchant amounts for year=" + year,
                                            responseList);

                                    return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                            .map(v -> {
                                                logger.info("Cached yearly merchant amounts by apiKey for year: {}", year);
                                                return response;
                                            });
                                });
                    });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch yearly merchant amounts for year={} apiKey={}", year, apiKey, e);
        });
    }
}
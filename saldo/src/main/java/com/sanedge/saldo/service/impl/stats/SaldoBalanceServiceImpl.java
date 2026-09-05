package com.sanedge.saldo.service.impl.stats;

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
import com.sanedge.saldo.domain.response.stats.balance.SaldoMonthBalanceResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoYearBalanceResponse;
import com.sanedge.saldo.repository.stats.SaldoBalanceRepository;
import com.sanedge.saldo.service.stats.SaldoBalanceService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SaldoBalanceServiceImpl implements SaldoBalanceService {
    private static final Logger logger = LoggerFactory.getLogger(SaldoBalanceServiceImpl.class);

    private final SaldoBalanceRepository saldoBalanceRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public SaldoBalanceServiceImpl(SaldoBalanceRepository saldoBalanceRepository,
                                  RedisService redisService,
                                  ObjectMapper objectMapper,
                                  TracingMetrics tracingMetrics) {
        this.saldoBalanceRepository = saldoBalanceRepository;
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
    public Uni<ApiResponse<List<SaldoMonthBalanceResponse>>> getMonthBalance(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = "saldo-stats:balance:monthly:" + year;

        Attributes attrs = Attributes.builder()
                .put("service.name", "saldo-balance-service")
                .put("operation", "get_month_balance")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.<ApiResponse<List<SaldoMonthBalanceResponse>>>traceAndMeasure(
                "getMonthBalance", "get_month_balance", attrs, () ->

                redisService.getReactive(cacheKey)
                        .chain(cachedJson -> {
                            if (cachedJson != null) {
                                logger.info("Cache HIT for key: {}", cacheKey);
                                ApiResponse<List<SaldoMonthBalanceResponse>> response = fromJson(cachedJson,
                                        new TypeReference<ApiResponse<List<SaldoMonthBalanceResponse>>>() {
                                        });
                                return Uni.createFrom().item(response);
                            }

                            logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                            return saldoBalanceRepository.findMonthlySaldoBalances(year)
                                    .chain(monthBalances -> {
                                        List<SaldoMonthBalanceResponse> responseList = monthBalances.stream()
                                                .map(SaldoMonthBalanceResponse::from)
                                                .collect(Collectors.toList());

                                        ApiResponse<List<SaldoMonthBalanceResponse>> response = ApiResponse.success(
                                                "Successfully fetched monthly saldo balance",
                                                responseList
                                        );

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> {
                                                    logger.info("Cached monthly saldo balance for year: {}", year);
                                                    return response;
                                                });
                                    });
                        })
                        .onFailure().recoverWithItem(e -> {
                            logger.error("❌ Failed to fetch monthly saldo balance for year={}", year, e);
                            return new ApiResponse<>("error", "Failed to fetch monthly saldo balance", Collections.emptyList());
                        }));
    }

    @Override
    public Uni<ApiResponse<List<SaldoYearBalanceResponse>>> getYearBalance(Long year) {
        try {
            validateYear(year);
        } catch (IllegalArgumentException e) {
            return Uni.createFrom().item(new ApiResponse<>("error", e.getMessage(), Collections.emptyList()));
        }

        String cacheKey = "saldo-stats:balance:yearly:" + year;

        Attributes attrs = Attributes.builder()
                .put("service.name", "saldo-balance-service")
                .put("operation", "get_year_balance")
                .put("year", String.valueOf(year))
                .build();

        return tracingMetrics.<ApiResponse<List<SaldoYearBalanceResponse>>>traceAndMeasure(
                "getYearBalance", "get_year_balance", attrs, () ->

                redisService.getReactive(cacheKey)
                        .chain(cachedJson -> {
                            if (cachedJson != null) {
                                logger.info("Cache HIT for key: {}", cacheKey);
                                ApiResponse<List<SaldoYearBalanceResponse>> response = fromJson(cachedJson,
                                        new TypeReference<ApiResponse<List<SaldoYearBalanceResponse>>>() {
                                        });
                                return Uni.createFrom().item(response);
                            }

                            logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                            return saldoBalanceRepository.findYearlySaldoBalances(year)
                                    .chain(yearBalances -> {
                                        List<SaldoYearBalanceResponse> responseList = yearBalances.stream()
                                                .map(SaldoYearBalanceResponse::from)
                                                .collect(Collectors.toList());

                                        ApiResponse<List<SaldoYearBalanceResponse>> response = ApiResponse.success(
                                                "Successfully fetched yearly saldo balance",
                                                responseList
                                        );

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> {
                                                    logger.info("Cached yearly saldo balance for year: {}", year);
                                                    return response;
                                                });
                                    });
                        })
                        .onFailure().recoverWithItem(e -> {
                            logger.error("❌ Failed to fetch yearly saldo balance for year={}", year, e);
                            return new ApiResponse<>("error", "Failed to fetch yearly saldo balance", Collections.emptyList());
                        }));
    }
}

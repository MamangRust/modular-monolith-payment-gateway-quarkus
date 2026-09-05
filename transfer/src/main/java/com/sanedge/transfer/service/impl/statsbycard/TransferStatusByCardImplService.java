package com.sanedge.transfer.service.impl.statsbycard;

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
import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.transfer.domain.requests.statsbycard.YearStatusTransferCardNumber;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.transfer.repository.statsbycard.TransferStatusByCardRepository;
import com.sanedge.transfer.service.stats.status.TransferStatusByCardService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransferStatusByCardImplService implements TransferStatusByCardService {
    private static final Logger logger = LoggerFactory.getLogger(TransferStatusByCardImplService.class);

    private final TransferStatusByCardRepository transferStatusByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransferStatusByCardImplService(TransferStatusByCardRepository transferStatusByCardRepository,
                                           RedisService redisService,
                                           ObjectMapper objectMapper,
                                           TracingMetrics tracingMetrics) {
        this.transferStatusByCardRepository = transferStatusByCardRepository;
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
    public Uni<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthStatusSuccessByCard(MonthStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly SUCCESS transfer status for card: {}, year: {}, month: {}", cardNumber, year, month);

        if (!isValidYearMonth(year, month) || cardNumber == null || cardNumber.isBlank()) {
            logger.error("❌ Invalid input parameters: year={}, month={}, cardNumber={}", year, month, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:status:month:success:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_status_success_by_card")
                .put("cardNumber", cardNumber)
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
                        return Uni.createFrom().item(ApiResponse.success("Monthly SUCCESS transfer status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusSuccessByCard", "find_month_status_success_by_card", attrs,
                            () -> transferStatusByCardRepository.findMonthTransferStatusSuccessByCard(req)
                                    .chain(results -> {
                                        List<TransferResponseMonthStatusSuccess> response = results.stream()
                                                .map(TransferResponseMonthStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly SUCCESS transfer status by card fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch monthly SUCCESS transfer status for card={}, year={}, month={}", cardNumber, year, month, e);
                                        return new ApiResponse<>("error", "Monthly SUCCESS transfer status by card fetching failed", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(YearStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching yearly SUCCESS transfer status for card: {}, year: {}", cardNumber, year);

        if (!isValidYear(year) || cardNumber == null || cardNumber.isBlank()) {
            logger.error("❌ Invalid input parameters: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:status:year:success:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_status_success_by_card")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseYearStatusSuccess> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseYearStatusSuccess>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly SUCCESS transfer status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusSuccessByCard", "find_yearly_status_success_by_card", attrs,
                            () -> transferStatusByCardRepository.findYearlyTransferStatusSuccessByCard(cardNumber, year)
                                    .chain(results -> {
                                        List<TransferResponseYearStatusSuccess> response = results.stream()
                                                .map(TransferResponseYearStatusSuccess::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly SUCCESS transfer status by card fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly SUCCESS transfer status for card={}, year={}", cardNumber, year, e);
                                        return new ApiResponse<>("error", "Yearly SUCCESS transfer status by card fetching failed", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthStatusFailedByCard(MonthStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();
        int month = req.getMonth();

        logger.info("📊 Fetching monthly FAILED transfer status for card: {}, year: {}, month: {}", cardNumber, year, month);

        if (!isValidYearMonth(year, month) || cardNumber == null || cardNumber.isBlank()) {
            logger.error("❌ Invalid input parameters: year={}, month={}, cardNumber={}", year, month, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:status:month:failed:%s:%d:%d", cardNumber, year, month);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_month_status_failed_by_card")
                .put("cardNumber", cardNumber)
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
                        return Uni.createFrom().item(ApiResponse.success("Monthly FAILED transfer status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthStatusFailedByCard", "find_month_status_failed_by_card", attrs,
                            () -> transferStatusByCardRepository.findMonthTransferStatusFailedByCard(req)
                                    .chain(results -> {
                                        List<TransferResponseMonthStatusFailed> response = results.stream()
                                                .map(TransferResponseMonthStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly FAILED transfer status by card fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch monthly FAILED transfer status for card={}, year={}, month={}", cardNumber, year, month, e);
                                        return new ApiResponse<>("error", "Monthly FAILED transfer status by card fetching failed", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailedByCard(YearStatusTransferCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching yearly FAILED transfer status for card: {}, year: {}", cardNumber, year);

        if (!isValidYear(year) || cardNumber == null || cardNumber.isBlank()) {
            logger.error("❌ Invalid input parameters: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:status:year:failed:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_status_failed_by_card")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferResponseYearStatusFailed> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferResponseYearStatusFailed>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly FAILED transfer status by card fetched successfully", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyStatusFailedByCard", "find_yearly_status_failed_by_card", attrs,
                            () -> transferStatusByCardRepository.findYearlyTransferStatusFailedByCard(cardNumber, year)
                                    .chain(results -> {
                                        List<TransferResponseYearStatusFailed> response = results.stream()
                                                .map(TransferResponseYearStatusFailed::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly FAILED transfer status by card fetched successfully", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly FAILED transfer status for card={}, year={}", cardNumber, year, e);
                                        return new ApiResponse<>("error", "Yearly FAILED transfer status by card fetching failed", Collections.emptyList());
                                    }));
                });
    }
}

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
import com.sanedge.transfer.domain.requests.statsbycard.MonthYearCardNumber;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.repository.statsbycard.TransferAmountByCardRepository;
import com.sanedge.transfer.service.stats.amount.TransferAmountByCardService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TransferAmountByCardImplService implements TransferAmountByCardService {
    private static final Logger logger = LoggerFactory.getLogger(TransferAmountByCardImplService.class);

    private final TransferAmountByCardRepository transferAmountByCardRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long STATS_CACHE_TTL_SECONDS = 300;

    @Inject
    public TransferAmountByCardImplService(TransferAmountByCardRepository transferAmountByCardRepository,
                                           RedisService redisService,
                                           ObjectMapper objectMapper,
                                           TracingMetrics tracingMetrics) {
        this.transferAmountByCardRepository = transferAmountByCardRepository;
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

    private boolean isValidYearAndCard(Long year, String cardNumber) {
        return year != null && year >= 1 && year <= 9999 && cardNumber != null && !cardNumber.isBlank();
    }

    @Override
    public Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsBySender(MonthYearCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching monthly transfer amounts (SENDER) for year={}, cardNumber={}", year, cardNumber);

        if (!isValidYearAndCard(year, cardNumber)) {
            logger.error("❌ Invalid year or cardNumber provided: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:amount:month:sender:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_monthly_amounts_by_sender")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferMonthAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferMonthAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Monthly transfer amounts (sender) retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthlyAmountsBySender", "find_monthly_amounts_by_sender", attrs,
                            () -> transferAmountByCardRepository.findMonthlyTransferAmountsBySender(cardNumber, year)
                                    .chain(amounts -> {
                                        List<TransferMonthAmountResponse> response = amounts.stream()
                                                .map(TransferMonthAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly transfer amounts (sender) retrieved successfully!", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch monthly transfer amounts (sender) for year={}, cardNumber={}", year, cardNumber, e);
                                        return new ApiResponse<>("error", "Failed to fetch monthly transfer amounts (sender)", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsByReceiver(MonthYearCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching monthly transfer amounts (RECEIVER) for year={}, cardNumber={}", year, cardNumber);

        if (!isValidYearAndCard(year, cardNumber)) {
            logger.error("❌ Invalid year or cardNumber provided: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:amount:month:receiver:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_monthly_amounts_by_receiver")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferMonthAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferMonthAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Monthly transfer amounts (receiver) retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findMonthlyAmountsByReceiver", "find_monthly_amounts_by_receiver", attrs,
                            () -> transferAmountByCardRepository.findMonthlyTransferAmountsByReceiver(cardNumber, year)
                                    .chain(amounts -> {
                                        List<TransferMonthAmountResponse> response = amounts.stream()
                                                .map(TransferMonthAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Monthly transfer amounts (receiver) retrieved successfully!", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch monthly transfer amounts (receiver) for year={}, cardNumber={}", year, cardNumber, e);
                                        return new ApiResponse<>("error", "Failed to fetch monthly transfer amounts (receiver)", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsBySender(MonthYearCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching yearly transfer amounts (SENDER) until year={}, cardNumber={}", year, cardNumber);

        if (!isValidYearAndCard(year, cardNumber)) {
            logger.error("❌ Invalid year or cardNumber provided: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:amount:year:sender:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_amounts_by_sender")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferYearAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferYearAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly transfer amounts (sender) retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyAmountsBySender", "find_yearly_amounts_by_sender", attrs,
                            () -> transferAmountByCardRepository.findYearlyTransferAmountsBySender(cardNumber, year)
                                    .chain(amounts -> {
                                        List<TransferYearAmountResponse> response = amounts.stream()
                                                .map(TransferYearAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly transfer amounts (sender) retrieved successfully!", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly transfer amounts (sender) until year={}, cardNumber={}", year, cardNumber, e);
                                        return new ApiResponse<>("error", "Failed to fetch yearly transfer amounts (sender)", Collections.emptyList());
                                    }));
                });
    }

    @Override
    public Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsByReceiver(MonthYearCardNumber req) {
        String cardNumber = req.getCardNumber();
        Long year = (long) req.getYear();

        logger.info("📊 Fetching yearly transfer amounts (RECEIVER) until year={}, cardNumber={}", year, cardNumber);

        if (!isValidYearAndCard(year, cardNumber)) {
            logger.error("❌ Invalid year or cardNumber provided: year={}, cardNumber={}", year, cardNumber);
            return Uni.createFrom().item(new ApiResponse<>("error", "Invalid input parameters", Collections.emptyList()));
        }

        String cacheKey = String.format("transfers:statsbycard:amount:year:receiver:%s:%d", cardNumber, year);

        Attributes attrs = Attributes.builder()
                .put("operation", "find_yearly_amounts_by_receiver")
                .put("cardNumber", cardNumber)
                .put("year", String.valueOf(year))
                .build();

        return redisService.getReactive(cacheKey)
                .chain(cachedJson -> {
                    if (cachedJson != null) {
                        logger.info("Cache HIT for key: {}", cacheKey);
                        List<TransferYearAmountResponse> cached = fromJson(cachedJson,
                                new TypeReference<List<TransferYearAmountResponse>>() {
                                });
                        return Uni.createFrom().item(ApiResponse.success("Yearly transfer amounts (receiver) retrieved successfully!", cached));
                    }

                    logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);
                    return tracingMetrics.traceAndMeasure("findYearlyAmountsByReceiver", "find_yearly_amounts_by_receiver", attrs,
                            () -> transferAmountByCardRepository.findYearlyTransferAmountsByReceiver(cardNumber, year)
                                    .chain(amounts -> {
                                        List<TransferYearAmountResponse> response = amounts.stream()
                                                .map(TransferYearAmountResponse::from)
                                                .collect(Collectors.toList());

                                        return redisService.setWithExpirationReactive(cacheKey, toJson(response), STATS_CACHE_TTL_SECONDS)
                                                .map(v -> ApiResponse.success("Yearly transfer amounts (receiver) retrieved successfully!", response));
                                    })
                                    .onFailure().recoverWithItem(e -> {
                                        logger.error("❌ Failed to fetch yearly transfer amounts (receiver) until year={}, cardNumber={}", year, cardNumber, e);
                                        return new ApiResponse<>("error", "Failed to fetch yearly transfer amounts (receiver)", Collections.emptyList());
                                    }));
                });
    }
}

package com.sanedge.merchant.service.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactions;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.common.domain.response.ApiResponsePagination;
import com.sanedge.common.domain.response.PagedResult;
import com.sanedge.common.domain.response.PaginationMeta;
import com.sanedge.merchant.domain.response.MerchantTransactionResponse;
import com.sanedge.merchant.repository.MerchantTransactionRepository;
import com.sanedge.merchant.service.MerchantTransactionService;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantTransactionServiceImpl implements MerchantTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantTransactionServiceImpl.class);

    private final MerchantTransactionRepository merchantTransactionRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final TracingMetrics tracingMetrics;

    private static final long LIST_CACHE_TTL_SECONDS = 300;

    @Inject
    public MerchantTransactionServiceImpl(MerchantTransactionRepository merchantTransactionRepository,
                    RedisService redisService,
                    ObjectMapper objectMapper,
                    TracingMetrics tracingMetrics) {
        this.merchantTransactionRepository = merchantTransactionRepository;
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
            logger.error("Error deserializing JSON to object with TypeReference", e);
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    @Override
    public Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findAll(FindAllMerchantTransactions req) {
        String cacheKey = String.format("merchant-tx:all:%d:%d:%s", req.getPage(), req.getPageSize(),
                        req.getSearch());

        Attributes attrs = Attributes.builder()
                        .put("operation", "find_all_merchant_transactions")
                        .put("service.name", "merchant-transaction-service")
                        .build();

        return tracingMetrics.<ApiResponsePagination<List<MerchantTransactionResponse>>>traceAndMeasure(
                        "findAllMerchantTransactions", "find_all_merchant_transactions", attrs, () -> {
                                return redisService.getReactive(cacheKey)
                                                .chain(cachedJson -> {
                                                        if (cachedJson != null) {
                                                                logger.info("Cache HIT for key: {}", cacheKey);
                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = fromJson(cachedJson,
                                                                                new TypeReference<ApiResponsePagination<List<MerchantTransactionResponse>>>() {
                                                                                });
                                                                return Uni.createFrom().item(response);
                                                        }

                                                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                                                        return merchantTransactionRepository.findAllTransactions(req)
                                                                        .chain(pagedResult -> {
                                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = buildPaginatedResponse(
                                                                                                pagedResult, req.getPage(), req.getPageSize(),
                                                                                                "Merchant transactions retrieved successfully",
                                                                                                MerchantTransactionResponse::from);

                                                                                return redisService.setWithExpirationReactive(cacheKey, toJson(response),
                                                                                                LIST_CACHE_TTL_SECONDS)
                                                                                                .map(v -> {
                                                                                                        logger.info("Cached response for key: {}", cacheKey);
                                                                                                        logger.info("Successfully retrieved {} merchant transactions",
                                                                                                                        pagedResult.getTotalRecords());
                                                                                                        return response;
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("Error finding all merchant transactions", e);
                        });
    }

    @Override
    public Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findById(FindAllMerchantTransactionsById req) {
        String cacheKey = String.format("merchant-tx:id:%d:%d:%d:%s", req.getMerchantId(), req.getPage(),
                        req.getPageSize(), req.getSearch());

        Attributes attrs = Attributes.builder()
                        .put("operation", "find_merchant_transactions_by_id")
                        .put("service.name", "merchant-transaction-service")
                        .put("merchant.id", String.valueOf(req.getMerchantId()))
                        .build();

        return tracingMetrics.<ApiResponsePagination<List<MerchantTransactionResponse>>>traceAndMeasure(
                        "findMerchantTransactionsById", "find_merchant_transactions_by_id", attrs, () -> {
                                return redisService.getReactive(cacheKey)
                                                .chain(cachedJson -> {
                                                        if (cachedJson != null) {
                                                                logger.info("Cache HIT for key: {}", cacheKey);
                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = fromJson(cachedJson,
                                                                                new TypeReference<ApiResponsePagination<List<MerchantTransactionResponse>>>() {
                                                                                });
                                                                return Uni.createFrom().item(response);
                                                        }

                                                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                                                        return merchantTransactionRepository.findAllTransactionsByMerchant(req)
                                                                        .chain(pagedResult -> {
                                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = buildPaginatedResponse(
                                                                                                pagedResult, req.getPage(), req.getPageSize(),
                                                                                                "Merchant transactions retrieved successfully",
                                                                                                MerchantTransactionResponse::from);

                                                                                return redisService.setWithExpirationReactive(cacheKey, toJson(response),
                                                                                                LIST_CACHE_TTL_SECONDS)
                                                                                                .map(v -> {
                                                                                                        logger.info("Cached response for key: {}", cacheKey);
                                                                                                        logger.info(
                                                                                                                        "Successfully retrieved {} merchant transactions for merchant id {}",
                                                                                                                        pagedResult.getTotalRecords(), req.getMerchantId());
                                                                                                        return response;
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("Error finding merchant transactions by merchant id", e);
                        });
    }

    @Override
    public Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findByApiKey(FindAllMerchantTransactionsByApiKey req) {
        String cacheKey = String.format("merchant-tx:apikey:%s:%d:%d:%s", req.getApiKey(), req.getPage(),
                        req.getPageSize(), req.getSearch());

        Attributes attrs = Attributes.builder()
                        .put("operation", "find_merchant_transactions_by_api_key")
                        .put("service.name", "merchant-transaction-service")
                        .build();

        return tracingMetrics.<ApiResponsePagination<List<MerchantTransactionResponse>>>traceAndMeasure(
                        "findMerchantTransactionsByApiKey", "find_merchant_transactions_by_api_key", attrs, () -> {
                                return redisService.getReactive(cacheKey)
                                                .chain(cachedJson -> {
                                                        if (cachedJson != null) {
                                                                logger.info("Cache HIT for key: {}", cacheKey);
                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = fromJson(cachedJson,
                                                                                new TypeReference<ApiResponsePagination<List<MerchantTransactionResponse>>>() {
                                                                                });
                                                                return Uni.createFrom().item(response);
                                                        }

                                                        logger.info("Cache MISS for key: {}. Fetching from DB.", cacheKey);

                                                        return merchantTransactionRepository.findAllTransactionsByApiKey(req)
                                                                        .chain(pagedResult -> {
                                                                                ApiResponsePagination<List<MerchantTransactionResponse>> response = buildPaginatedResponse(
                                                                                                pagedResult, req.getPage(), req.getPageSize(),
                                                                                                "Merchant transactions retrieved successfully",
                                                                                                MerchantTransactionResponse::from);

                                                                                return redisService.setWithExpirationReactive(cacheKey, toJson(response),
                                                                                                LIST_CACHE_TTL_SECONDS)
                                                                                                .map(v -> {
                                                                                                        logger.info("Cached response for key: {}", cacheKey);
                                                                                                        logger.info("Successfully retrieved {} merchant transactions for api key",
                                                                                                                        pagedResult.getTotalRecords());
                                                                                                        return response;
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("Error finding merchant transactions by api key", e);
                        });
    }

    private <T, R> ApiResponsePagination<List<R>> buildPaginatedResponse(
                    PagedResult<T> pagedResult,
                    int pageReq,
                    int pageSizeReq,
                    String successMessage,
                    Function<T, R> mapper) {

        List<R> data = pagedResult.getData().stream()
                        .map(mapper)
                        .collect(Collectors.toList());

        int totalRecords = pagedResult.getTotalRecords();
        int size = pageSizeReq > 0 ? pageSizeReq : 1;
        int totalPages = (int) Math.ceil((double) totalRecords / size);

        PaginationMeta pagination = new PaginationMeta(pageReq, size, totalPages, totalRecords);

        return new ApiResponsePagination<>("success", successMessage, data, pagination);
    }
}

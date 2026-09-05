package com.sanedge.merchant.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponsePagination;
import com.sanedge.common.domain.response.PagedResult;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactions;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.merchant.domain.response.MerchantTransactionResponse;
import com.sanedge.merchant.entity.MerchantTransactions;
import com.sanedge.merchant.repository.MerchantTransactionRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantTransactionServiceImplTest {

        @Mock
        private MerchantTransactionRepository merchantTransactionRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private MerchantTransactionServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantTransactionServiceImpl(
                                merchantTransactionRepository,
                                redisService,
                                objectMapper,
                                tracingMetrics);

                // Lenient stubs to execute the supplier directly
                lenient().doAnswer(invocation -> {
                        Supplier<Uni<?>> supplier = invocation.getArgument(3);
                        return supplier.get();
                }).when(tracingMetrics).traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
        }

        private MerchantTransactions createMockTransaction(Integer id) {
                // Use the all-args constructor based on MerchantTransactions class
                return new MerchantTransactions(
                                id, // transactionId
                                "CARD-1234", // cardNumber
                                150000, // amount
                                "CREDIT", // paymentMethod
                                1, // merchantId
                                "Test Merchant", // merchantName
                                Timestamp.valueOf(LocalDateTime.of(2024, 6, 15, 10, 30)), // transactionTime
                                Timestamp.valueOf(LocalDateTime.now()), // createdAt
                                Timestamp.valueOf(LocalDateTime.now()), // updatedAt
                                null // deletedAt
                );
        }

        private String toJson(Object obj) {
                try {
                        return objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                }
        }

        private FindAllMerchantTransactions findAllReq(int page, int size, String search) {
                FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                req.setPage(page);
                req.setPageSize(size);
                req.setSearch(search == null ? "" : search);
                return req;
        }

        private FindAllMerchantTransactionsById findByIdReq(Long merchantId, int page, int size, String search) {
                FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                req.setMerchantId(merchantId);
                req.setPage(page);
                req.setPageSize(size);
                req.setSearch(search == null ? "" : search);
                return req;
        }

        private FindAllMerchantTransactionsByApiKey findByApiKeyReq(String apiKey, int page, int size, String search) {
                FindAllMerchantTransactionsByApiKey req = new FindAllMerchantTransactionsByApiKey();
                req.setApiKey(apiKey);
                req.setPage(page);
                req.setPageSize(size);
                req.setSearch(search == null ? "" : search);
                return req;
        }

        @Nested
        @DisplayName("findAll tests")
        class FindAllTests {

                @Test
                @DisplayName("cache miss - fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        FindAllMerchantTransactions req = findAllReq(1, 10, "");
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(merchantTransactionRepository.findAllTransactions(any(FindAllMerchantTransactions.class)))
                                        .thenReturn(Uni.createFrom()
                                                        .item(new PagedResult<>(List.of(createMockTransaction(1)), 1)));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findAll(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getCardNumber()).isEqualTo("CARD-1234");
                }

                @Test
                @DisplayName("cache hit - return cached response without DB call")
                void cacheHit_returnsCached() {
                        FindAllMerchantTransactions req = findAllReq(1, 10, "");
                        MerchantTransactionResponse cachedData = MerchantTransactionResponse
                                        .from(createMockTransaction(1));
                        ApiResponsePagination<List<MerchantTransactionResponse>> cached = new ApiResponsePagination<>(
                                        "success", "Merchant transactions retrieved successfully", List.of(cachedData),
                                        null);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findAll(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findById tests")
        class FindByIdTests {

                @Test
                @DisplayName("cache miss - fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        FindAllMerchantTransactionsById req = findByIdReq(1L, 1, 10, "");
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(merchantTransactionRepository
                                        .findAllTransactionsByMerchant(any(FindAllMerchantTransactionsById.class)))
                                        .thenReturn(Uni.createFrom()
                                                        .item(new PagedResult<>(List.of(createMockTransaction(1)), 1)));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findById(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - return cached response")
                void cacheHit_returnsCached() {
                        FindAllMerchantTransactionsById req = findByIdReq(1L, 1, 10, "");
                        MerchantTransactionResponse cachedData = MerchantTransactionResponse
                                        .from(createMockTransaction(1));
                        ApiResponsePagination<List<MerchantTransactionResponse>> cached = new ApiResponsePagination<>(
                                        "success", "Merchant transactions retrieved successfully", List.of(cachedData),
                                        null);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findById(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findByApiKey tests")
        class FindByApiKeyTests {

                @Test
                @DisplayName("cache miss - fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        FindAllMerchantTransactionsByApiKey req = findByApiKeyReq("key-123", 1, 10, "");
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(merchantTransactionRepository
                                        .findAllTransactionsByApiKey(any(FindAllMerchantTransactionsByApiKey.class)))
                                        .thenReturn(Uni.createFrom()
                                                        .item(new PagedResult<>(List.of(createMockTransaction(1)), 1)));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - return cached response")
                void cacheHit_returnsCached() {
                        FindAllMerchantTransactionsByApiKey req = findByApiKeyReq("key-123", 1, 10, "");
                        MerchantTransactionResponse cachedData = MerchantTransactionResponse
                                        .from(createMockTransaction(1));
                        ApiResponsePagination<List<MerchantTransactionResponse>> cached = new ApiResponsePagination<>(
                                        "success", "Merchant transactions retrieved successfully", List.of(cachedData),
                                        null);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponsePagination<List<MerchantTransactionResponse>> result = service.findByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
package com.sanedge.transaction.service.impl.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;
import com.sanedge.transaction.entity.TransactionMonthMethod;
import com.sanedge.transaction.entity.TransactionYearMethod;
import com.sanedge.transaction.repository.stats.TransactionMethodRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransactionMethodImplServiceTest {

        @Mock
        private TransactionMethodRepository transactionMethodRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransactionMethodImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransactionMethodImplService(
                                transactionMethodRepository,
                                redisService,
                                objectMapper,
                                tracingMetrics);

                lenient().doAnswer(invocation -> {
                        Supplier<Uni<?>> supplier = invocation.getArgument(3);
                        return supplier.get();
                }).when(tracingMetrics)
                                .traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
        }

        private String toJson(Object obj) {
                try {
                        return objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to serialize in test helper", e);
                }
        }

        @Nested
        @DisplayName("findMonthlyMethod tests")
        class FindMonthlyMethodTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionMonthMethodResponse>> result = service.findMonthlyMethod(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transactionMethodRepository.findMonthlyPaymentMethods(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionMonthMethod("Jan", "CREDIT", 5, 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionMonthMethodResponse>> result = service.findMonthlyMethod(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TransactionMonthMethodResponse> cachedData = List.of(
                                        new TransactionMonthMethodResponse("Jan", "CREDIT", 5L, 1000L));
                        when(redisService.getReactive("transactions:stats:method:month:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionMonthMethodResponse>> result = service.findMonthlyMethod(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyMethod tests")
        class FindYearlyMethodTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionYearMethodResponse>> result = service.findYearlyMethod(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transactionMethodRepository.findYearlyPaymentMethods(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionYearMethod("2024", "DEBIT", 3, 2000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionYearMethodResponse>> result = service.findYearlyMethod(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TransactionYearMethodResponse> cachedData = List.of(
                                        new TransactionYearMethodResponse("2024", "DEBIT", 3L, 2000L));
                        when(redisService.getReactive("transactions:stats:method:year:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionYearMethodResponse>> result = service.findYearlyMethod(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
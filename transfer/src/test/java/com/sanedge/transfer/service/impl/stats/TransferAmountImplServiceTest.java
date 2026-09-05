package com.sanedge.transfer.service.impl.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.entity.TransferMonthAmount;
import com.sanedge.transfer.entity.TransferYearAmount;
import com.sanedge.transfer.repository.stats.TransferAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransferAmountImplServiceTest {

        @Mock
        private TransferAmountRepository transferAmountRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransferAmountImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransferAmountImplService(
                                transferAmountRepository,
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
        @DisplayName("findMonthlyAmounts tests")
        class FindMonthlyAmountsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmounts(null)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when year is zero")
                void zeroYear_returnsError() {
                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmounts(0L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferAmountRepository.findMonthlyTransferAmounts(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferMonthAmount("Jan", 100000L),
                                                        new TransferMonthAmount("Feb", 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(100000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TransferMonthAmountResponse> cachedData = List.of(
                                        new TransferMonthAmountResponse("Jan", 100000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to retrieve monthly transfer amounts");
                }
        }

        @Nested
        @DisplayName("findYearlyAmounts tests")
        class FindYearlyAmountsTests {

                @Test
                @DisplayName("should return error when year is invalid")
                void invalidYear_returnsError() {
                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmounts(10000L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferAmountRepository.findYearlyTransferAmounts(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferYearAmount("2023", 500000L),
                                                        new TransferYearAmount("2024", 700000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(1).getReportYear()).isEqualTo("2024");
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TransferYearAmountResponse> cachedData = List.of(
                                        new TransferYearAmountResponse("2024", 700000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to retrieve yearly transfer amounts");
                }
        }
}
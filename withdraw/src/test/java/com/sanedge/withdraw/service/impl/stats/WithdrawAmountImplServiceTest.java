package com.sanedge.withdraw.service.impl.stats;

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
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.entity.WithdrawMonthlyAmount;
import com.sanedge.withdraw.entity.WithdrawYearlyAmount;
import com.sanedge.withdraw.repository.stats.WithdrawAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class WithdrawAmountImplServiceTest {

        @Mock
        private WithdrawAmountRepository withdrawAmountRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private WithdrawAmountImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new WithdrawAmountImplService(
                                withdrawAmountRepository,
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
        @DisplayName("findMonthlyWithdraws tests")
        class FindMonthlyWithdrawsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(null)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Invalid year provided");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when year is zero")
                void zeroYear_returnsError() {
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(0L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Invalid year provided");
                }

                @Test
                @DisplayName("should return error when year exceeds 9999")
                void invalidYear_returnsError() {
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(10000L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Invalid year provided");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawAmountRepository.findMonthlyWithdraws(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawMonthlyAmount("Jan", 100000L),
                                                        new WithdrawMonthlyAmount("Feb", 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(100000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<WithdrawMonthlyAmountResponse> cachedData = List.of(
                                        new WithdrawMonthlyAmountResponse("Jan", 100000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(2024L)
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

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service.findMonthlyWithdraws(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to retrieve monthly withdraw amounts");
                        assertThat(result.data()).isEmpty();
                }
        }

        @Nested
        @DisplayName("findYearlyWithdraws tests")
        class FindYearlyWithdrawsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service.findYearlyWithdraws(null)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Invalid year provided");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawAmountRepository.findYearlyWithdraws(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawYearlyAmount("2023", 500000L),
                                                        new WithdrawYearlyAmount("2024", 700000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service.findYearlyWithdraws(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(1).getReportYear()).isEqualTo("2024");
                        assertThat(result.data().get(1).getTotalAmount()).isEqualTo(700000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<WithdrawYearlyAmountResponse> cachedData = List.of(
                                        new WithdrawYearlyAmountResponse("2024", 700000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service.findYearlyWithdraws(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getReportYear()).isEqualTo("2024");
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service.findYearlyWithdraws(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to retrieve yearly withdraw amounts");
                }
        }
}
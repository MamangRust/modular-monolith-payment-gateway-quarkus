package com.sanedge.saldo.service.impl.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.sanedge.saldo.domain.requests.MonthTotalSaldoBalance;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoMonthTotalBalanceResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoYearTotalBalanceResponse;
import com.sanedge.saldo.entity.SaldoMonthTotalBalance;
import com.sanedge.saldo.entity.SaldoYearTotalBalance;
import com.sanedge.saldo.repository.stats.SaldoTotalAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class SaldoTotalAmountServiceImplTest {

        @Mock
        private SaldoTotalAmountRepository saldoTotalAmountRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private SaldoTotalAmountServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new SaldoTotalAmountServiceImpl(
                                saldoTotalAmountRepository,
                                redisService,
                                objectMapper,
                                tracingMetrics);
                // Lenient stubbing to execute the supplier directly
                lenient().doAnswer(invocation -> {
                        Supplier<Uni<?>> supplier = invocation.getArgument(3);
                        return supplier.get();
                }).when(tracingMetrics).traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
        }

        private String toJson(Object obj) {
                try {
                        return objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                }
        }

        private MonthTotalSaldoBalance monthReq(Long year, int month) {
                MonthTotalSaldoBalance req = new MonthTotalSaldoBalance();
                req.setYear(year);
                req.setMonth(month);
                return req;
        }

        @Nested
        @DisplayName("findMonthTotalBalance tests")
        class FindMonthTotalBalanceTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<SaldoMonthTotalBalanceResponse>> result = service
                                        .findMonthTotalBalance(monthReq(null, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when month is invalid")
                void invalidMonth_returnsError() {
                        ApiResponse<List<SaldoMonthTotalBalanceResponse>> result = service
                                        .findMonthTotalBalance(monthReq(2024L, 13))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthTotalSaldoBalance req = monthReq(2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(saldoTotalAmountRepository.findMonthlyTotalSaldoBalance(anyLong(), anyInt(), anyLong(),
                                        anyInt()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new SaldoMonthTotalBalance("2024", "Jun", 500000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<SaldoMonthTotalBalanceResponse>> result = service
                                        .findMonthTotalBalance(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jun");
                        assertThat(result.data().get(0).getTotalBalance()).isEqualTo(500000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthTotalSaldoBalance req = monthReq(2024L, 6);
                        ApiResponse<List<SaldoMonthTotalBalanceResponse>> cached = ApiResponse.success(
                                        "Get monthly total balance success",
                                        List.of(new SaldoMonthTotalBalanceResponse("2024", "Jun", 500000L)));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<SaldoMonthTotalBalanceResponse>> result = service
                                        .findMonthTotalBalance(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearTotalBalance tests")
        class FindYearTotalBalanceTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<SaldoYearTotalBalanceResponse>> result = service.findYearTotalBalance(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(saldoTotalAmountRepository.findYearlyTotalSaldoBalance(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new SaldoYearTotalBalance("2024", 1000000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<SaldoYearTotalBalanceResponse>> result = service.findYearTotalBalance(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getReportYear()).isEqualTo("2024");
                        assertThat(result.data().get(0).getTotalBalance()).isEqualTo(1000000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        ApiResponse<List<SaldoYearTotalBalanceResponse>> cached = ApiResponse.success(
                                        "Get yearly total balance success",
                                        List.of(new SaldoYearTotalBalanceResponse("2024", 1000000L)));
                        when(redisService.getReactive("saldo-stats:total-balance:yearly:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<SaldoYearTotalBalanceResponse>> result = service.findYearTotalBalance(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
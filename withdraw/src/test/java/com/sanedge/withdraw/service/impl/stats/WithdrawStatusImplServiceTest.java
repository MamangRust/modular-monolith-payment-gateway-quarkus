package com.sanedge.withdraw.service.impl.stats;

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
import com.sanedge.withdraw.domain.requests.MonthStatusWithdraw;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.entity.WithdrawMonthStatusFailed;
import com.sanedge.withdraw.entity.WithdrawMonthStatusSuccess;
import com.sanedge.withdraw.entity.WithdrawYearStatusFailed;
import com.sanedge.withdraw.entity.WithdrawYearStatusSuccess;
import com.sanedge.withdraw.repository.stats.WithdrawStatusRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class WithdrawStatusImplServiceTest {

        @Mock
        private WithdrawStatusRepository withdrawStatusRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private WithdrawStatusImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new WithdrawStatusImplService(
                                withdrawStatusRepository,
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

        private MonthStatusWithdraw buildMonthReq(Long year, int month) {
                MonthStatusWithdraw req = new MonthStatusWithdraw();
                req.setYear(year);
                req.setMonth(month);
                return req;
        }

        @Nested
        @DisplayName("findMonthStatusSuccess tests")
        class FindMonthStatusSuccessTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        MonthStatusWithdraw req = buildMonthReq(null, 6);
                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when month is zero")
                void zeroMonth_returnsError() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 0);
                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when month exceeds 12")
                void invalidMonth_returnsError() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 13);
                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 6);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawStatusRepository.findMonthWithdrawStatusSuccess(anyLong(), anyInt(), anyLong(),
                                        anyInt()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawMonthStatusSuccess("2024", "Jun", 5, 500000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jun");
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(500000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 6);

                        List<WithdrawResponseMonthStatusSuccess> cachedData = List.of(
                                        new WithdrawResponseMonthStatusSuccess("2024", "Jun", 300000L, 5L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 6);

                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

                        ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to fetch monthly SUCCESS withdraw status");
                }
        }

        @Nested
        @DisplayName("findYearlyStatusSuccess tests")
        class FindYearlyStatusSuccessTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(null).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawStatusRepository.findYearlyWithdrawStatusSuccess(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawYearStatusSuccess("2024", 10, 1000000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(2024L).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getReportYear()).isEqualTo("2024");
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(10);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<WithdrawResponseYearStatusSuccess> cachedData = List.of(
                                        new WithdrawResponseYearStatusSuccess("2024", 1000000L, 10L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(2024L).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findMonthStatusFailed tests")
        class FindMonthStatusFailedTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        MonthStatusWithdraw req = buildMonthReq(null, 6);
                        ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 6);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawStatusRepository.findMonthWithdrawStatusFailed(anyLong(), anyInt(), anyLong(),
                                        anyInt()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawMonthStatusFailed("2024", "Jun", 2, 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(2);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(200000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusWithdraw req = buildMonthReq(2024L, 6);

                        List<WithdrawResponseMonthStatusFailed> cachedData = List.of(
                                        new WithdrawResponseMonthStatusFailed("2024", "Jun", 200000L, 2L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusFailed tests")
        class FindYearlyStatusFailedTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(null).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawStatusRepository.findYearlyWithdrawStatusFailed(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawYearStatusFailed("2024", 3, 300000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(2024L).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(3);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<WithdrawResponseYearStatusFailed> cachedData = List.of(
                                        new WithdrawResponseYearStatusFailed("2024", 300000L, 3L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(2024L).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
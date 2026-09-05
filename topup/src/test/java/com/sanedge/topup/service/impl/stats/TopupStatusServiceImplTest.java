package com.sanedge.topup.service.impl.stats;

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
import com.sanedge.topup.domain.requests.stats.MonthTopupStatus;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.topup.entity.TopupMonthStatusFailed;
import com.sanedge.topup.entity.TopupMonthStatusSuccess;
import com.sanedge.topup.entity.TopupYearStatusFailed;
import com.sanedge.topup.entity.TopupYearStatusSuccess;
import com.sanedge.topup.repository.stats.TopupStatusRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupStatusServiceImplTest {

        @Mock
        private TopupStatusRepository topupStatusRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TopupStatusServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupStatusServiceImpl(topupStatusRepository, redisService, objectMapper, tracingMetrics);
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

        private MonthTopupStatus buildMonthReq(Long year, int month) {
                MonthTopupStatus req = new MonthTopupStatus();
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
                        MonthTopupStatus req = buildMonthReq(null, 6);
                        ApiResponse<List<TopupResponseMonthStatusSuccess>> result = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when month is invalid")
                void invalidMonth_returnsError() {
                        MonthTopupStatus req = buildMonthReq(2024L, 13);
                        ApiResponse<List<TopupResponseMonthStatusSuccess>> result = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthTopupStatus req = buildMonthReq(2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupStatusRepository.findMonthTopupStatusSuccess(anyLong(), anyInt(), anyLong(),
                                        anyInt()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TopupMonthStatusSuccess("2024", "Jun", 5, 50000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseMonthStatusSuccess>> result = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthTopupStatus req = buildMonthReq(2024L, 6);
                        List<TopupResponseMonthStatusSuccess> cachedData = List.of(
                                        new TopupResponseMonthStatusSuccess("2024", "Jun", 50000L, 5L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupResponseMonthStatusSuccess>> result = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusSuccess tests")
        class FindYearlyStatusSuccessTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TopupResponseYearStatusSuccess>> result = service.findYearlyStatusSuccess(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupStatusRepository.findYearlyTopupStatusSuccess(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TopupYearStatusSuccess("2024", 10, 100000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(10);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupResponseYearStatusSuccess> cachedData = List.of(
                                        new TopupResponseYearStatusSuccess("2024", 100000L, 10L));
                        when(redisService.getReactive("topups:stats:year:success:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findMonthStatusFailed tests")
        class FindMonthStatusFailedTests {

                @Test
                @DisplayName("should return error when month exceeds 12")
                void invalidMonth_returnsError() {
                        MonthTopupStatus req = buildMonthReq(2024L, 13);
                        ApiResponse<List<TopupResponseMonthStatusFailed>> result = service.findMonthStatusFailed(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthTopupStatus req = buildMonthReq(2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupStatusRepository.findMonthTopupStatusFailed(anyLong(), anyInt(), anyLong(), anyInt()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TopupMonthStatusFailed("2024", "Jun", 2, 20000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseMonthStatusFailed>> result = service.findMonthStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(2);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthTopupStatus req = buildMonthReq(2024L, 6);
                        List<TopupResponseMonthStatusFailed> cachedData = List.of(
                                        new TopupResponseMonthStatusFailed("2024", "Jun", 20000L, 2L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupResponseMonthStatusFailed>> result = service.findMonthStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusFailed tests")
        class FindYearlyStatusFailedTests {

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupStatusRepository.findYearlyTopupStatusFailed(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TopupYearStatusFailed("2024", 3, 30000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseYearStatusFailed>> result = service.findYearlyStatusFailed(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(3);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupResponseYearStatusFailed> cachedData = List.of(
                                        new TopupResponseYearStatusFailed("2024", 30000L, 3L));
                        when(redisService.getReactive("topups:stats:year:failed:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupResponseYearStatusFailed>> result = service.findYearlyStatusFailed(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
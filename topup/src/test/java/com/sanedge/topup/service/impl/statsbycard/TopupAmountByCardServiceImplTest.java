package com.sanedge.topup.service.impl.statsbycard;

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
import com.sanedge.topup.domain.requests.statsbycard.MonthTopupStatusCardNumber;
import com.sanedge.topup.domain.requests.statsbycard.YearTopupStatusCardNumber;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.topup.entity.TopupMonthStatusFailed;
import com.sanedge.topup.entity.TopupMonthStatusSuccess;
import com.sanedge.topup.entity.TopupYearStatusFailed;
import com.sanedge.topup.entity.TopupYearStatusSuccess;
import com.sanedge.topup.repository.statsbycard.TopupStatusByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupStatusByCardServiceImplTest {

        @Mock
        private TopupStatusByCardRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TopupStatusByCardServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupStatusByCardServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        private MonthTopupStatusCardNumber monthReq(String card, Long year, int month) {
                MonthTopupStatusCardNumber r = new MonthTopupStatusCardNumber();
                r.setCardNumber(card);
                r.setYear(year);
                r.setMonth(month);
                return r;
        }

        private YearTopupStatusCardNumber yearReq(String card, Long year) {
                YearTopupStatusCardNumber r = new YearTopupStatusCardNumber();
                r.setCardNumber(card);
                r.setYear(year);
                return r;
        }

        @Nested
        @DisplayName("findMonthStatusSuccess tests")
        class FindMonthStatusSuccessTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupResponseMonthStatusSuccess>> r = service
                                        .findMonthStatusSuccess(monthReq(null, 2024L, 6)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void invalidMonth_returnsError() {
                        ApiResponse<List<TopupResponseMonthStatusSuccess>> r = service
                                        .findMonthStatusSuccess(monthReq("CARD", 2024L, 13)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthTopupStatusCardNumber req = monthReq("CARD-1", 2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthTopupStatusSuccessByCard(any()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TopupMonthStatusSuccess("2024", "Jun", 5, 50000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseMonthStatusSuccess>> r = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                        assertThat(r.data().get(0).getTotalSuccess()).isEqualTo(5);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthTopupStatusCardNumber req = monthReq("CARD-1", 2024L, 6);
                        List<TopupResponseMonthStatusSuccess> cached = List
                                        .of(new TopupResponseMonthStatusSuccess("2024", "Jun", 50000L, 5L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupResponseMonthStatusSuccess>> r = service.findMonthStatusSuccess(req)
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusSuccess tests")
        class FindYearlyStatusSuccessTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupResponseYearStatusSuccess>> r = service
                                        .findYearlyStatusSuccess(yearReq(null, 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTopupStatusSuccessByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(
                                                        List.of(new TopupYearStatusSuccess("2024", 10, 100000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseYearStatusSuccess>> r = service
                                        .findYearlyStatusSuccess(yearReq("CARD-1", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupResponseYearStatusSuccess> cached = List
                                        .of(new TopupResponseYearStatusSuccess("2024", 100000L, 10L));
                        when(redisService.getReactive("topups:stats:card:status:year:success:CARD-1:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupResponseYearStatusSuccess>> r = service
                                        .findYearlyStatusSuccess(yearReq("CARD-1", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findMonthStatusFailed tests")
        class FindMonthStatusFailedTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupResponseMonthStatusFailed>> r = service
                                        .findMonthStatusFailed(monthReq(null, 2024L, 6)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthTopupStatusCardNumber req = monthReq("CARD-1", 2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthTopupStatusFailedByCard(any()))
                                        .thenReturn(Uni.createFrom().item(
                                                        List.of(new TopupMonthStatusFailed("2024", "Jun", 2, 20000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseMonthStatusFailed>> r = service.findMonthStatusFailed(req).await()
                                        .indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                        assertThat(r.data().get(0).getTotalFailed()).isEqualTo(2);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthTopupStatusCardNumber req = monthReq("CARD-1", 2024L, 6);
                        List<TopupResponseMonthStatusFailed> cached = List
                                        .of(new TopupResponseMonthStatusFailed("2024", "Jun", 20000L, 2L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupResponseMonthStatusFailed>> r = service.findMonthStatusFailed(req).await()
                                        .indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearlyStatusFailed tests")
        class FindYearlyStatusFailedTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupResponseYearStatusFailed>> r = service
                                        .findYearlyStatusFailed(yearReq(null, 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTopupStatusFailedByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new TopupYearStatusFailed("2024", 3, 30000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupResponseYearStatusFailed>> r = service
                                        .findYearlyStatusFailed(yearReq("CARD-1", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).isNotNull();
                        assertThat(r.data()).hasSize(1);
                        assertThat(r.data().get(0).getTotalFailed()).isEqualTo(3);
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupResponseYearStatusFailed> cached = List
                                        .of(new TopupResponseYearStatusFailed("2024", 30000L, 3L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupResponseYearStatusFailed>> r = service
                                        .findYearlyStatusFailed(yearReq("CARD-1", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }
}
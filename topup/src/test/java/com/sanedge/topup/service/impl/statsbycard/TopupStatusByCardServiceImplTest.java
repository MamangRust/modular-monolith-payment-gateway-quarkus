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
import com.sanedge.topup.domain.requests.stats.YearMonthMethod;
import com.sanedge.topup.domain.response.stats.amount.TopupMonthAmountResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupYearlyAmountResponse;
import com.sanedge.topup.entity.TopupMonthAmount;
import com.sanedge.topup.entity.TopupYearAmount;
import com.sanedge.topup.repository.statsbycard.TopupAmountByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupAmountByCardServiceImplTest {

        @Mock
        private TopupAmountByCardRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private TopupAmountByCardServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupAmountByCardServiceImpl(repository, redisService, objectMapper, tracingMetrics);
                lenient().doAnswer(inv -> {
                        Supplier<Uni<?>> s = inv.getArgument(3);
                        return s.get();
                })
                                .when(tracingMetrics)
                                .traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
        }

        private String toJson(Object o) {
                try {
                        return objectMapper.writeValueAsString(o);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                }
        }

        private YearMonthMethod req(String card, Long year) {
                YearMonthMethod r = new YearMonthMethod();
                r.setCardNumber(card);
                r.setYear(year);
                return r;
        }

        @Nested
        @DisplayName("findMonthlyAmounts tests")
        class FindMonthlyAmountsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupMonthAmountResponse>> r = service.findMonthlyAmounts(req(null, 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyTopupAmountsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new TopupMonthAmount("Jan", 2000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupMonthAmountResponse>> r = service.findMonthlyAmounts(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                        assertThat(r.data().get(0).getTotalAmount()).isEqualTo(2000L);
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupMonthAmountResponse> cached = List.of(new TopupMonthAmountResponse("Jan", 2000L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupMonthAmountResponse>> r = service.findMonthlyAmounts(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearlyAmounts tests")
        class FindYearlyAmountsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupYearlyAmountResponse>> r = service.findYearlyAmounts(req(null, 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTopupAmountsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new TopupYearAmount("2024", 12000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupYearlyAmountResponse>> r = service.findYearlyAmounts(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupYearlyAmountResponse> cached = List.of(new TopupYearlyAmountResponse("2024", 12000L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupYearlyAmountResponse>> r = service.findYearlyAmounts(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }
}
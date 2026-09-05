package com.sanedge.card.service.impl.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;
import com.sanedge.card.entity.CardMonthAmount;
import com.sanedge.card.entity.CardYearAmount;
import com.sanedge.card.repository.statsbycard.CardWithdrawAmountByCardRepository;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardWithdrawAmountByCardServiceImplTest {

        @Mock
        private CardWithdrawAmountByCardRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private CardWithdrawAmountByCardServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new CardWithdrawAmountByCardServiceImpl(repository, redisService, objectMapper,
                                tracingMetrics);
                lenient().doAnswer(inv -> {
                        Supplier<Uni<?>> s = inv.getArgument(3);
                        return s.get();
                })
                                .when(tracingMetrics)
                                .traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
        }

        private String toJson(Object obj) {
                try {
                        return objectMapper.writeValueAsString(obj);
                } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                }
        }

        private MonthYearCardNumberCard validReq() {
                MonthYearCardNumberCard req = new MonthYearCardNumberCard();
                req.setCardNumber("CARD-1");
                req.setYear(2024L);
                return req;
        }

        @Nested
        @DisplayName("findMonthAmountByCard tests")
        class FindMonthAmountByCardTests {
                @Test
                void nullRequest_returnsError() {
                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountByCard(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumberCard req = validReq();
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getMonthlyWithdrawAmountByCard(anyString(), any(LocalDate.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardMonthAmount("Jun", 6000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearCardNumberCard req = validReq();
                        ApiResponse<List<CardResponseMonthAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseMonthAmount("Jun", 6000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmountByCard tests")
        class FindYearAmountByCardTests {
                @Test
                void nullRequest_returnsError() {
                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountByCard(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumberCard req = validReq();
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getYearlyWithdrawAmountByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardYearAmount("2024", 9000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearCardNumberCard req = validReq();
                        ApiResponse<List<CardResponseYearAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseYearAmount("2024", 9000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
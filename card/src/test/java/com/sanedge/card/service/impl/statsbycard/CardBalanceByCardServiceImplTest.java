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
import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;
import com.sanedge.card.entity.CardMonthBalance;
import com.sanedge.card.entity.CardYearBalance;
import com.sanedge.card.repository.statsbycard.CardBalanceByCardRepository;
import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardBalanceByCardServiceImplTest {

        @Mock
        private CardBalanceByCardRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private CardBalanceByCardServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new CardBalanceByCardServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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
        @DisplayName("findMonthBalanceByCard tests")
        class FindMonthBalanceByCardTests {
                @Test
                void nullRequest_returnsError() {
                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalanceByCard(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumberCard req = validReq();
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getMonthlyBalancesByCard(any(LocalDate.class), anyString()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardMonthBalance("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalanceByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearCardNumberCard req = validReq();
                        ApiResponse<List<CardResponseMonthBalance>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseMonthBalance("Jan", 1000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalanceByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearBalanceByCard tests")
        class FindYearBalanceByCardTests {
                @Test
                void nullRequest_returnsError() {
                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalanceByCard(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumberCard req = validReq();
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getYearlyBalancesByCard(anyLong(), anyString()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardYearBalance("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalanceByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearCardNumberCard req = validReq();
                        ApiResponse<List<CardResponseYearBalance>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseYearBalance("2024", 5000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalanceByCard(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
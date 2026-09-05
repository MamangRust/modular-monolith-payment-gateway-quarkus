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
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;
import com.sanedge.topup.entity.TopupMonthMethod;
import com.sanedge.topup.entity.TopupYearMethod;
import com.sanedge.topup.repository.statsbycard.TopupMethodByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupMethodByCardServiceImplTest {

        @Mock
        private TopupMethodByCardRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private TopupMethodByCardServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupMethodByCardServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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
        @DisplayName("findMonthlyMethods tests")
        class FindMonthlyMethodsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupMonthMethodResponse>> r = service.findMonthlyMethods(req(null, 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyTopupMethodsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TopupMonthMethod("Jan", "BANK_TRANSFER", 1, 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupMonthMethodResponse>> r = service.findMonthlyMethods(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupMonthMethodResponse> cached = List
                                        .of(new TopupMonthMethodResponse("Jan", "BANK_TRANSFER", 1L, 5000L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupMonthMethodResponse>> r = service.findMonthlyMethods(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearlyMethods tests")
        class FindYearlyMethodsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TopupYearlyMethodResponse>> r = service.findYearlyMethods(req(null, 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTopupMethodsByCardId(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TopupYearMethod("2024", "BANK_TRANSFER", 2, 10000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupYearlyMethodResponse>> r = service.findYearlyMethods(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }

                @Test
                void cacheHit_returnsCached() {
                        List<TopupYearlyMethodResponse> cached = List
                                        .of(new TopupYearlyMethodResponse("2024", "BANK_TRANSFER", 2L, 10000L));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<TopupYearlyMethodResponse>> r = service.findYearlyMethods(req("CARD", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }
}
package com.sanedge.transaction.service.impl.statsbycard;

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
import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;
import com.sanedge.transaction.entity.TransactionMonthMethod;
import com.sanedge.transaction.entity.TransactionYearMethod;
import com.sanedge.transaction.repository.statsbycard.TransactionMethodByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransactionMethodByCardImplServiceTest {

        @Mock
        private TransactionMethodByCardRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private TransactionMethodByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransactionMethodByCardImplService(repository, redisService, objectMapper,
                                tracingMetrics);
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

        private MonthYearPaymentMethod req(String card, Long year) {
                MonthYearPaymentMethod r = new MonthYearPaymentMethod();
                r.setCardNumber(card);
                r.setYear(year);
                return r;
        }

        @Nested
        @DisplayName("findMonthlyMethod tests")
        class FindMonthlyMethodTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionMonthMethodResponse>> r = service
                                        .findMonthlyMethod(req(null, 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyPaymentMethodsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TransactionMonthMethod("Jan", "CREDIT", 1, 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());
                        ApiResponse<List<TransactionMonthMethodResponse>> r = service.findMonthlyMethod(req("x", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearlyMethod tests")
        class FindYearlyMethodTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionYearMethodResponse>> r = service.findYearlyMethod(req(null, 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyPaymentMethodsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(
                                                        List.of(new TransactionYearMethod("2024", "DEBIT", 2, 2000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());
                        ApiResponse<List<TransactionYearMethodResponse>> r = service.findYearlyMethod(req("x", 2024L))
                                        .await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                }
        }
}
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
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;
import com.sanedge.transaction.entity.TransactionMonthAmount;
import com.sanedge.transaction.entity.TransactionYearAmount;
import com.sanedge.transaction.repository.statsbycard.TransactionAmountByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransactionAmountByCardImplServiceTest {

        @Mock
        private TransactionAmountByCardRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransactionAmountByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransactionAmountByCardImplService(repository, redisService, objectMapper,
                                tracingMetrics);
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

        private MonthYearPaymentMethod req(String card, Long year) {
                MonthYearPaymentMethod r = new MonthYearPaymentMethod();
                r.setCardNumber(card);
                r.setYear(year);
                return r;
        }

        @Nested
        @DisplayName("findMonthlyAmounts tests")
        class FindMonthlyAmountsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionMonthAmountResponse>> r = service
                                        .findMonthlyAmounts(req(null, 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyAmountsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new TransactionMonthAmount("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionMonthAmountResponse>> r = service
                                        .findMonthlyAmounts(req("1234", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyAmounts tests")
        class FindYearlyAmountsTests {
                @Test
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionYearlyAmountResponse>> r = service
                                        .findYearlyAmounts(req(null, 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyAmountsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new TransactionYearAmount("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionYearlyAmountResponse>> r = service
                                        .findYearlyAmounts(req("1234", 2024L)).await().indefinitely();
                        assertThat(r.status()).isEqualTo("success");
                        assertThat(r.data()).hasSize(1);
                }
        }
}
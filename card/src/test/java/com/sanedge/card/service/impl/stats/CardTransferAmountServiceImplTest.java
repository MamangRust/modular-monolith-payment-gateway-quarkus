package com.sanedge.card.service.impl.stats;

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
import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;
import com.sanedge.card.entity.CardMonthAmount;
import com.sanedge.card.entity.CardYearAmount;
import com.sanedge.card.repository.stats.CardTransferAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardTransferAmountServiceImplTest {

        @Mock
        private CardTransferAmountRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private CardTransferAmountServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new CardTransferAmountServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        @Nested
        @DisplayName("findMonthAmountSender tests")
        class FindMonthAmountSenderTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountSender(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getMonthlyTransferAmountSender(any(LocalDate.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardMonthAmount("Apr", 4000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountSender(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseMonthAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseMonthAmount("Apr", 4000L)));
                        when(redisService.getReactive("card-transfer:month-sender:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountSender(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmountSender tests")
        class FindYearAmountSenderTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountSender(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getYearlyTransferAmountSender(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardYearAmount("2024", 10000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountSender(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseYearAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseYearAmount("2024", 10000L)));
                        when(redisService.getReactive("card-transfer:year-sender:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountSender(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findMonthAmountReceiver tests")
        class FindMonthAmountReceiverTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountReceiver(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getMonthlyTransferAmountReceiver(any(LocalDate.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardMonthAmount("May", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountReceiver(2024L)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseMonthAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseMonthAmount("May", 5000L)));
                        when(redisService.getReactive("card-transfer:month-receiver:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseMonthAmount>> result = service.findMonthAmountReceiver(2024L)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmountReceiver tests")
        class FindYearAmountReceiverTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountReceiver(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getYearlyTransferAmountReceiver(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardYearAmount("2024", 12000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountReceiver(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseYearAmount>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseYearAmount("2024", 12000L)));
                        when(redisService.getReactive("card-transfer:year-receiver:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseYearAmount>> result = service.findYearAmountReceiver(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
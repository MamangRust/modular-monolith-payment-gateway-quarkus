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
import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;
import com.sanedge.card.entity.CardMonthBalance;
import com.sanedge.card.entity.CardYearBalance;
import com.sanedge.card.repository.stats.CardBalanceRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardBalanceServiceImplTest {

        @Mock
        private CardBalanceRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private CardBalanceServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new CardBalanceServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        @Nested
        @DisplayName("findMonthBalance tests")
        class FindMonthBalanceTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalance(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getMonthlyBalances(any(LocalDate.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardMonthBalance("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalance(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonth()).isEqualTo("Jan");
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseMonthBalance>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseMonthBalance("Jan", 1000L)));
                        when(redisService.getReactive("card-balance:month:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseMonthBalance>> result = service.findMonthBalance(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearBalance tests")
        class FindYearBalanceTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalance(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.getYearlyBalances(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new CardYearBalance("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalance(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<CardResponseYearBalance>> cached = ApiResponse.success("Success",
                                        List.of(new CardResponseYearBalance("2024", 5000L)));
                        when(redisService.getReactive("card-balance:year:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<CardResponseYearBalance>> result = service.findYearBalance(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
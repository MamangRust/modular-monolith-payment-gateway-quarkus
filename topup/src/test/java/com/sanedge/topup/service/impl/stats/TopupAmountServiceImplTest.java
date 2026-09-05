package com.sanedge.topup.service.impl.stats;

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
import com.sanedge.topup.domain.response.stats.amount.TopupMonthAmountResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupYearlyAmountResponse;
import com.sanedge.topup.entity.TopupMonthAmount;
import com.sanedge.topup.entity.TopupYearAmount;
import com.sanedge.topup.repository.stats.TopupAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupAmountServiceImplTest {

        @Mock
        private TopupAmountRepository topupAmountRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TopupAmountServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupAmountServiceImpl(topupAmountRepository, redisService, objectMapper, tracingMetrics);
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

        @Nested
        @DisplayName("findMonthlyAmounts tests")
        class FindMonthlyAmountsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TopupMonthAmountResponse>> result = service.findMonthlyAmounts(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupAmountRepository.findMonthlyTopupAmounts(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new TopupMonthAmount("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupMonthAmountResponse>> result = service.findMonthlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(1000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupMonthAmountResponse> cachedData = List.of(new TopupMonthAmountResponse("Jan", 1000L));
                        when(redisService.getReactive("topups:stats:amount:month:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupMonthAmountResponse>> result = service.findMonthlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyAmounts tests")
        class FindYearlyAmountsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TopupYearlyAmountResponse>> result = service.findYearlyAmounts(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupAmountRepository.findYearlyTopupAmounts(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(new TopupYearAmount("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupYearlyAmountResponse>> result = service.findYearlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupYearlyAmountResponse> cachedData = List
                                        .of(new TopupYearlyAmountResponse("2024", 5000L));
                        when(redisService.getReactive("topups:stats:amount:year:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupYearlyAmountResponse>> result = service.findYearlyAmounts(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
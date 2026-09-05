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
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;
import com.sanedge.topup.entity.TopupMonthMethod;
import com.sanedge.topup.entity.TopupYearMethod;
import com.sanedge.topup.repository.stats.TopupMethodRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupMethodServiceImplTest {

        @Mock
        private TopupMethodRepository topupMethodRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TopupMethodServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TopupMethodServiceImpl(topupMethodRepository, redisService, objectMapper, tracingMetrics);
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
        @DisplayName("findMonthlyMethods tests")
        class FindMonthlyMethodsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TopupMonthMethodResponse>> result = service.findMonthlyMethods(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupMethodRepository.findMonthlyTopupMethods(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TopupMonthMethod("Jan", "BANK_TRANSFER", 5, 10000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupMonthMethodResponse>> result = service.findMonthlyMethods(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupMonthMethodResponse> cachedData = List.of(
                                        new TopupMonthMethodResponse("Jan", "BANK_TRANSFER", 5L, 10000L));
                        when(redisService.getReactive("topups:stats:method:month:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupMonthMethodResponse>> result = service.findMonthlyMethods(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyMethods tests")
        class FindYearlyMethodsTests {

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TopupYearlyMethodResponse>> result = service.findYearlyMethods(null)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(topupMethodRepository.findYearlyTopupMethods(anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new TopupYearMethod("2024", "BANK_TRANSFER", 10, 20000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TopupYearlyMethodResponse>> result = service.findYearlyMethods(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        List<TopupYearlyMethodResponse> cachedData = List.of(
                                        new TopupYearlyMethodResponse("2024", "BANK_TRANSFER", 10L, 20000L));
                        when(redisService.getReactive("topups:stats:method:year:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TopupYearlyMethodResponse>> result = service.findYearlyMethods(2024L)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
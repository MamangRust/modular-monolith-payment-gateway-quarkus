package com.sanedge.merchant.service.impl.stats;

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
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.merchant.entity.MerchantMonthlyAmount;
import com.sanedge.merchant.entity.MerchantYearlyAmount;
import com.sanedge.merchant.repository.stats.MerchantAmountRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantAmountServiceImplTest {

        @Mock
        private MerchantAmountRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private MerchantAmountServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantAmountServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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
        @DisplayName("findMonthAmount tests")
        class FindMonthAmountTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmount(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyAmount(anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantMonthlyAmount("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmount(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<MerchantResponseMonthlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseMonthlyAmount("Jan", 1000L)));
                        when(redisService.getReactive("merchant-stats:amount:monthly:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmount(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmount tests")
        class FindYearAmountTests {
                @Test
                void nullYear_returnsError() {
                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmount(null).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyAmount(anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantYearlyAmount("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmount(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        ApiResponse<List<MerchantResponseYearlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseYearlyAmount("2024", 5000L)));
                        when(redisService.getReactive("merchant-stats:amount:yearly:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmount(2024L).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
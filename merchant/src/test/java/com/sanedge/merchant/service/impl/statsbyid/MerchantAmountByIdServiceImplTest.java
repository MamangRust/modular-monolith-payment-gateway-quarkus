package com.sanedge.merchant.service.impl.statsbyid;

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
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearAmountMerchant;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.merchant.entity.MerchantMonthlyAmount;
import com.sanedge.merchant.entity.MerchantYearlyAmount;
import com.sanedge.merchant.repository.statsbyid.MerchantAmountByIdRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantAmountByIdServiceImplTest {

        @Mock
        private MerchantAmountByIdRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private MerchantAmountByIdServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantAmountByIdServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        private MonthYearAmountMerchant createReq(Long merchantId, int year) {
                MonthYearAmountMerchant req = new MonthYearAmountMerchant();
                req.setMerchantId(merchantId);
                req.setYear((long) year);
                return req;
        }

        @Nested
        @DisplayName("findMonthAmountById tests")
        class FindMonthAmountByIdTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service
                                        .findMonthAmountById(createReq(1L, 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearAmountMerchant req = createReq(1L, 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyAmountById(anyLong(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantMonthlyAmount("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmountById(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearAmountMerchant req = createReq(1L, 2024);
                        ApiResponse<List<MerchantResponseMonthlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseMonthlyAmount("Jan", 1000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmountById(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmountById tests")
        class FindYearAmountByIdTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service
                                        .findYearAmountById(createReq(1L, 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearAmountMerchant req = createReq(1L, 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyAmountById(anyLong(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantYearlyAmount("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmountById(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearAmountMerchant req = createReq(1L, 2024);
                        ApiResponse<List<MerchantResponseYearlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseYearlyAmount("2024", 5000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmountById(req).await()
                                        .indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
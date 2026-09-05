package com.sanedge.merchant.service.impl.statsbyid;

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
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearTotalAmountMerchant;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;
import com.sanedge.merchant.entity.MerchantMonthlyTotalAmount;
import com.sanedge.merchant.entity.MerchantYearlyTotalAmount;
import com.sanedge.merchant.repository.statsbyid.MerchantTotalAmountByIdRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantTotalAmountByIdServiceImplTest {

        @Mock
        private MerchantTotalAmountByIdRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private MerchantTotalAmountByIdServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantTotalAmountByIdServiceImpl(repository, redisService, objectMapper,
                                tracingMetrics);
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

        private MonthYearTotalAmountMerchant createReq(Long merchantId, int year) {
                MonthYearTotalAmountMerchant req = new MonthYearTotalAmountMerchant();
                req.setMerchantId(merchantId);
                req.setYear((long) year);
                return req;
        }

        @Nested
        @DisplayName("findMonthTotalAmountById tests")
        class FindMonthTotalAmountByIdTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseMonthlyTotalAmount>> result = service
                                        .findMonthTotalAmountById(createReq(1L, 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearTotalAmountMerchant req = createReq(1L, 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyTotalAmountById(anyLong(), any(LocalDate.class)))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new MerchantMonthlyTotalAmount("2024", "Jun", 500000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseMonthlyTotalAmount>> result = service
                                        .findMonthTotalAmountById(req).await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearTotalAmountMerchant req = createReq(1L, 2024);
                        ApiResponse<List<MerchantResponseMonthlyTotalAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseMonthlyTotalAmount("2024", "Jun", 500000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseMonthlyTotalAmount>> result = service
                                        .findMonthTotalAmountById(req).await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearTotalAmountById tests")
        class FindYearTotalAmountByIdTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseYearlyTotalAmount>> result = service
                                        .findYearTotalAmountById(createReq(1L, 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearTotalAmountMerchant req = createReq(1L, 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTotalAmountById(anyLong(), anyLong(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(
                                                        List.of(new MerchantYearlyTotalAmount("2024", 1000000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseYearlyTotalAmount>> result = service
                                        .findYearTotalAmountById(req).await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearTotalAmountMerchant req = createReq(1L, 2024);
                        ApiResponse<List<MerchantResponseYearlyTotalAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseYearlyTotalAmount("2024", 1000000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseYearlyTotalAmount>> result = service
                                        .findYearTotalAmountById(req).await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
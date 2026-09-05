package com.sanedge.merchant.service.impl.statsbyapikey;

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
import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearPaymentMethodApiKey;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;
import com.sanedge.merchant.entity.MerchantMonthlyPaymentMethod;
import com.sanedge.merchant.entity.MerchantYearlyPaymentMethod;
import com.sanedge.merchant.repository.statsbyapikey.MerchantMethodByApiKeyRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantMethodByApiKeyServiceImplTest {

        @Mock
        private MerchantMethodByApiKeyRepository repository;
        @Mock
        private RedisService redisService;
        @Mock
        private TracingMetrics tracingMetrics;
        private MerchantMethodByApiKeyServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantMethodByApiKeyServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        private MonthYearPaymentMethodApiKey createReq(String apiKey, int year) {
                MonthYearPaymentMethodApiKey req = new MonthYearPaymentMethodApiKey();
                req.setApiKey(apiKey);
                req.setYear((long) year);
                return req;
        }

        @Nested
        @DisplayName("findMonthMethod tests")
        class FindMonthMethodTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> result = service
                                        .findMonthMethod(createReq("key", 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearPaymentMethodApiKey req = createReq("key", 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyPaymentMethodsByApiKey(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List
                                                        .of(new MerchantMonthlyPaymentMethod("Jan", "CREDIT", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> result = service.findMonthMethod(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearPaymentMethodApiKey req = createReq("key", 2024);
                        ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseMonthlyPaymentMethod("Jan", "CREDIT", 5000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> result = service.findMonthMethod(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearMethod tests")
        class FindYearMethodTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseYearlyPaymentMethod>> result = service
                                        .findYearMethod(createReq("key", 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearPaymentMethodApiKey req = createReq("key", 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyPaymentMethodsByApiKey(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new MerchantYearlyPaymentMethod("2024", "CREDIT", 20000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseYearlyPaymentMethod>> result = service.findYearMethod(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearPaymentMethodApiKey req = createReq("key", 2024);
                        ApiResponse<List<MerchantResponseYearlyPaymentMethod>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseYearlyPaymentMethod("2024", "CREDIT", 20000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseYearlyPaymentMethod>> result = service.findYearMethod(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
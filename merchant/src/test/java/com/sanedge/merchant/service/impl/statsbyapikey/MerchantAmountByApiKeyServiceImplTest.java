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
import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearAmountApiKey;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.merchant.entity.MerchantMonthlyAmount;
import com.sanedge.merchant.entity.MerchantYearlyAmount;
import com.sanedge.merchant.repository.statsbyapikey.MerchantAmountByApiKeyRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantAmountByApiKeyServiceImplTest {

        @Mock
        private MerchantAmountByApiKeyRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private MerchantAmountByApiKeyServiceImpl service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new MerchantAmountByApiKeyServiceImpl(repository, redisService, objectMapper, tracingMetrics);
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

        private MonthYearAmountApiKey createReq(String apiKey, int year) {
                MonthYearAmountApiKey req = new MonthYearAmountApiKey();
                req.setApiKey(apiKey);
                req.setYear((long) year);
                return req;
        }

        @Nested
        @DisplayName("findMonthAmountByApiKey tests")
        class FindMonthAmountByApiKeyTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service
                                        .findMonthAmountByApiKey(createReq("key", 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearAmountApiKey req = createReq("key", 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthlyAmountByApiKey(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantMonthlyAmount("Jan", 1000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmountByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearAmountApiKey req = createReq("key", 2024);
                        ApiResponse<List<MerchantResponseMonthlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseMonthlyAmount("Jan", 1000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseMonthlyAmount>> result = service.findMonthAmountByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findYearAmountByApiKey tests")
        class FindYearAmountByApiKeyTests {
                @Test
                void invalidYear_returnsError() {
                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service
                                        .findYearAmountByApiKey(createReq("key", 0)).await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                void cacheMiss_fetchesFromDb() {
                        MonthYearAmountApiKey req = createReq("key", 2024);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyAmountByApiKey(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom()
                                                        .item(List.of(new MerchantYearlyAmount("2024", 5000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmountByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                void cacheHit_returnsCached() {
                        MonthYearAmountApiKey req = createReq("key", 2024);
                        ApiResponse<List<MerchantResponseYearlyAmount>> cached = ApiResponse.success("Success",
                                        List.of(new MerchantResponseYearlyAmount("2024", 5000L)));
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().item(toJson(cached)));

                        ApiResponse<List<MerchantResponseYearlyAmount>> result = service.findYearAmountByApiKey(req)
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("success");
                }
        }
}
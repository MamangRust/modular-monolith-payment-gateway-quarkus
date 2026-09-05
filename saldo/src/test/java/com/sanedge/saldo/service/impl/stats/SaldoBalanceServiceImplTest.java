package com.sanedge.saldo.service.impl.stats;

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
import com.sanedge.saldo.domain.response.stats.balance.SaldoMonthBalanceResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoYearBalanceResponse;
import com.sanedge.saldo.entity.SaldoMonthBalance;
import com.sanedge.saldo.entity.SaldoYearBalance;
import com.sanedge.saldo.repository.stats.SaldoBalanceRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class SaldoBalanceServiceImplTest {

    @Mock
    private SaldoBalanceRepository saldoBalanceRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private TracingMetrics tracingMetrics;

    private SaldoBalanceServiceImpl service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new SaldoBalanceServiceImpl(
                saldoBalanceRepository,
                redisService,
                objectMapper,
                tracingMetrics);
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
    @DisplayName("getMonthBalance tests")
    class GetMonthBalanceTests {

        @Test
        @DisplayName("should return error when year is null")
        void nullYear_returnsError() {
            ApiResponse<List<SaldoMonthBalanceResponse>> result = service.getMonthBalance(null)
                    .await().indefinitely();
            assertThat(result.status()).isEqualTo("error");
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(saldoBalanceRepository.findMonthlySaldoBalances(anyLong()))
                    .thenReturn(Uni.createFrom().item(List.of(new SaldoMonthBalance("Jan", 1000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<SaldoMonthBalanceResponse>> result = service.getMonthBalance(2024L)
                    .await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            ApiResponse<List<SaldoMonthBalanceResponse>> cached = ApiResponse.success(
                    "Successfully fetched monthly saldo balance",
                    List.of(new SaldoMonthBalanceResponse("Jan", 1000L)));
            when(redisService.getReactive("saldo-stats:balance:monthly:2024"))
                    .thenReturn(Uni.createFrom().item(toJson(cached)));

            ApiResponse<List<SaldoMonthBalanceResponse>> result = service.getMonthBalance(2024L)
                    .await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getYearBalance tests")
    class GetYearBalanceTests {

        @Test
        @DisplayName("should return error when year is null")
        void nullYear_returnsError() {
            ApiResponse<List<SaldoYearBalanceResponse>> result = service.getYearBalance(null)
                    .await().indefinitely();
            assertThat(result.status()).isEqualTo("error");
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(saldoBalanceRepository.findYearlySaldoBalances(anyLong()))
                    .thenReturn(Uni.createFrom().item(List.of(new SaldoYearBalance("2024", 5000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<SaldoYearBalanceResponse>> result = service.getYearBalance(2024L)
                    .await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            ApiResponse<List<SaldoYearBalanceResponse>> cached = ApiResponse.success(
                    "Successfully fetched yearly saldo balance",
                    List.of(new SaldoYearBalanceResponse("2024", 5000L)));
            when(redisService.getReactive("saldo-stats:balance:yearly:2024"))
                    .thenReturn(Uni.createFrom().item(toJson(cached)));

            ApiResponse<List<SaldoYearBalanceResponse>> result = service.getYearBalance(2024L)
                    .await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }
    }
}
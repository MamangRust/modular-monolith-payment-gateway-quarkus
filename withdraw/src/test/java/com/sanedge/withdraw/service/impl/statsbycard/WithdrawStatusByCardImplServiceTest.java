package com.sanedge.withdraw.service.impl.statsbycard;

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
import com.sanedge.withdraw.domain.requests.statsbycard.MonthStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.requests.statsbycard.YearStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.entity.WithdrawMonthStatusFailed;
import com.sanedge.withdraw.entity.WithdrawMonthStatusSuccess;
import com.sanedge.withdraw.entity.WithdrawYearStatusFailed;
import com.sanedge.withdraw.entity.WithdrawYearStatusSuccess;
import com.sanedge.withdraw.repository.statsbycard.WithdrawStatusByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class WithdrawStatusByCardImplServiceTest {

    @Mock
    private WithdrawStatusByCardRepository withdrawStatusByCardRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private TracingMetrics tracingMetrics;

    private WithdrawStatusByCardImplService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new WithdrawStatusByCardImplService(
                withdrawStatusByCardRepository,
                redisService,
                objectMapper,
                tracingMetrics);

        lenient().doAnswer(invocation -> {
            Supplier<Uni<?>> supplier = invocation.getArgument(3);
            return supplier.get();
        }).when(tracingMetrics)
                .traceAndMeasure(anyString(), anyString(), any(Attributes.class), any());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize in test helper", e);
        }
    }

    private MonthStatusWithdrawCardNumber buildMonthReq(String cardNumber, long year, int month) {
        MonthStatusWithdrawCardNumber req = new MonthStatusWithdrawCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear((int) year);
        req.setMonth(month);
        return req;
    }

    private YearStatusWithdrawCardNumber buildYearReq(String cardNumber, long year) {
        YearStatusWithdrawCardNumber req = new YearStatusWithdrawCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear((int) year);
        return req;
    }

    @Nested
    @DisplayName("findMonthStatusSuccessByCard tests")
    class FindMonthStatusSuccessByCardTests {

        @Test
        @DisplayName("should return error when cardNumber is null")
        void nullCardNumber_returnsError() {
            MonthStatusWithdrawCardNumber req = buildMonthReq(null, 2024, 6);
            ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                    .findMonthStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("should return error when month is invalid")
        void invalidMonth_returnsError() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 13);
            ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                    .findMonthStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(withdrawStatusByCardRepository.findMonthWithdrawStatusSuccessByCard(
                    any(MonthStatusWithdrawCardNumber.class)))
                    .thenReturn(Uni.createFrom().item(List.of(
                            new WithdrawMonthStatusSuccess("2024", "Jun", 5, 500000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                    .findMonthStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getMonthName()).isEqualTo("Jun");
            assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
            assertThat(result.data().get(0).getTotalAmount()).isEqualTo(500000L);
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

            List<WithdrawResponseMonthStatusSuccess> cachedData = List.of(
                    new WithdrawResponseMonthStatusSuccess("2024", "Jun", 300000L, 3L));
            when(redisService.getReactive(anyString()))
                    .thenReturn(Uni.createFrom().item(toJson(cachedData)));

            ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                    .findMonthStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(3);
        }

        @Test
        @DisplayName("on failure - should return error response")
        void onFailure_returnsErrorResponse() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

            when(redisService.getReactive(anyString()))
                    .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

            ApiResponse<List<WithdrawResponseMonthStatusSuccess>> result = service
                    .findMonthStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.message()).isEqualTo(
                    "Failed to fetch monthly SUCCESS withdraw status by card");
        }
    }

    @Nested
    @DisplayName("findYearlyStatusSuccessByCard tests")
    class FindYearlyStatusSuccessByCardTests {

        @Test
        @DisplayName("should return error when cardNumber is blank")
        void blankCardNumber_returnsError() {
            YearStatusWithdrawCardNumber req = buildYearReq("   ", 2024);
            ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                    .findYearlyStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            YearStatusWithdrawCardNumber req = buildYearReq("1234567890123456", 2024);

            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(withdrawStatusByCardRepository.findYearlyWithdrawStatusSuccessByCard(anyString(),
                    anyLong()))
                    .thenReturn(Uni.createFrom().item(List.of(
                            new WithdrawYearStatusSuccess("2024", 10, 1000000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                    .findYearlyStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(10);
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            YearStatusWithdrawCardNumber req = buildYearReq("1234567890123456", 2024);

            // FIX: Gunakan DTO Response, bukan Entity
            List<WithdrawResponseYearStatusSuccess> cachedData = List.of(
                    new WithdrawResponseYearStatusSuccess("2024", 1L, 300000L));
            when(redisService.getReactive(anyString()))
                    .thenReturn(Uni.createFrom().item(toJson(cachedData)));

            ApiResponse<List<WithdrawResponseYearStatusSuccess>> result = service
                    .findYearlyStatusSuccessByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findMonthStatusFailedByCard tests")
    class FindMonthStatusFailedByCardTests {

        @Test
        @DisplayName("should return error when cardNumber is null")
        void nullCardNumber_returnsError() {
            MonthStatusWithdrawCardNumber req = buildMonthReq(null, 2024, 6);
            ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                    .findMonthStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(withdrawStatusByCardRepository.findMonthWithdrawStatusFailedByCard(
                    any(MonthStatusWithdrawCardNumber.class)))
                    .thenReturn(Uni.createFrom().item(List.of(
                            // FIX: Sesuaikan mock data jadi 2, biar match sama assertion di bawah
                            new WithdrawMonthStatusFailed("2024", "Jun", 2, 300000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                    .findMonthStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getTotalFailed()).isEqualTo(2);
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            MonthStatusWithdrawCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

            // FIX: Gunakan DTO Response, bukan Entity
            List<WithdrawResponseMonthStatusFailed> cachedData = List.of(
                    new WithdrawResponseMonthStatusFailed("2024", "Jun", 3L, 300000L));
            when(redisService.getReactive(anyString()))
                    .thenReturn(Uni.createFrom().item(toJson(cachedData)));

            ApiResponse<List<WithdrawResponseMonthStatusFailed>> result = service
                    .findMonthStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findYearlyStatusFailedByCard tests")
    class FindYearlyStatusFailedByCardTests {

        @Test
        @DisplayName("should return error when cardNumber is null")
        void nullCardNumber_returnsError() {
            YearStatusWithdrawCardNumber req = buildYearReq(null, 2024);
            ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                    .findYearlyStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("error");
            assertThat(result.data()).isEmpty();
        }

        @Test
        @DisplayName("cache miss - should fetch from DB and cache result")
        void cacheMiss_fetchesFromDb() {
            YearStatusWithdrawCardNumber req = buildYearReq("1234567890123456", 2024);

            when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
            when(withdrawStatusByCardRepository.findYearlyWithdrawStatusFailedByCard(anyString(),
                    anyLong()))
                    .thenReturn(Uni.createFrom().item(List.of(
                            new WithdrawYearStatusFailed("2024", 3, 300000L))));
            when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                    .thenReturn(Uni.createFrom().voidItem());

            ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                    .findYearlyStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().get(0).getTotalFailed()).isEqualTo(3);
        }

        @Test
        @DisplayName("cache hit - should return cached response without DB call")
        void cacheHit_returnsCached() {
            YearStatusWithdrawCardNumber req = buildYearReq("1234567890123456", 2024);

            List<WithdrawResponseYearStatusFailed> cachedData = List.of(
                    new WithdrawResponseYearStatusFailed("2024", 3L, 300000L));
            when(redisService.getReactive(anyString()))
                    .thenReturn(Uni.createFrom().item(toJson(cachedData)));

            ApiResponse<List<WithdrawResponseYearStatusFailed>> result = service
                    .findYearlyStatusFailedByCard(req).await().indefinitely();

            assertThat(result.status()).isEqualTo("success");
            assertThat(result.data()).hasSize(1);
        }
    }
}
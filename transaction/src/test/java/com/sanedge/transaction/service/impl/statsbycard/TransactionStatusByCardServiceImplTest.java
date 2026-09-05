package com.sanedge.transaction.service.impl.statsbycard;

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
import com.sanedge.transaction.domain.requests.statsbycard.MonthStatusTransactionCardNumber;
import com.sanedge.transaction.domain.requests.statsbycard.YearStatusTransactionCardNumber;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;
import com.sanedge.transaction.entity.TransactionMonthStatusFailed;
import com.sanedge.transaction.entity.TransactionMonthStatusSuccess;
import com.sanedge.transaction.entity.TransactionYearStatusFailed;
import com.sanedge.transaction.entity.TransactionYearStatusSuccess;
import com.sanedge.transaction.repository.statsbycard.TransactionStatusByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransactionStatusByCardImplServiceTest {

        @Mock
        private TransactionStatusByCardRepository repository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransactionStatusByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransactionStatusByCardImplService(repository, redisService, objectMapper,
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
                        throw new RuntimeException("Failed to serialize in test helper", e);
                }
        }

        private MonthStatusTransactionCardNumber monthReq(String card, Long year, int month) {
                MonthStatusTransactionCardNumber req = new MonthStatusTransactionCardNumber();
                req.setCardNumber(card);
                req.setYear(year);
                req.setMonth(month);
                return req;
        }

        private YearStatusTransactionCardNumber yearReq(String card, Long year) {
                YearStatusTransactionCardNumber req = new YearStatusTransactionCardNumber();
                req.setCardNumber(card);
                req.setYear(year);
                return req;
        }

        @Nested
        @DisplayName("findMonthStatusSuccess tests")
        class FindMonthStatusSuccessTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(monthReq(null, 2024L, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when cardNumber is empty")
                void emptyCard_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(monthReq("   ", 2024L, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(monthReq("CARD-1", null, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when month is invalid (0)")
                void zeroMonth_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(monthReq("CARD-1", 2024L, 0))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when month is invalid (13)")
                void invalidMonth_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(monthReq("CARD-1", 2024L, 13))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusTransactionCardNumber req = monthReq("CARD-1", 2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthTransactionStatusSuccessByCard(any()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionMonthStatusSuccess("2024", "Jun", 5, 500000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(500000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusTransactionCardNumber req = monthReq("CARD-1", 2024L, 6);
                        List<TransactionResponseMonthStatusSuccess> cachedData = List.of(
                                        new TransactionResponseMonthStatusSuccess("2024", "Jun", 500000L, 5L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusSuccess tests")
        class FindYearlyStatusSuccessTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(yearReq(null, 2024L))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when cardNumber is empty")
                void emptyCard_returnsError() {
                        ApiResponse<List<TransactionResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(yearReq("   ", 2024L))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(yearReq("CARD-1", null))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearStatusTransactionCardNumber req = yearReq("CARD-1", 2024L);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTransactionStatusSuccessByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionYearStatusSuccess("2024", 10, 1000000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(10);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(1000000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearStatusTransactionCardNumber req = yearReq("CARD-1", 2024L);
                        List<TransactionResponseYearStatusSuccess> cachedData = List.of(
                                        new TransactionResponseYearStatusSuccess("2024", 1000000L, 10L));
                        when(redisService.getReactive("transactions:stats:status:card:year:success:CARD-1:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccess(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findMonthStatusFailed tests")
        class FindMonthStatusFailedTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(monthReq(null, 2024L, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(monthReq("CARD-1", null, 6))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when month exceeds 12")
                void invalidMonth_returnsError() {
                        ApiResponse<List<TransactionResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(monthReq("CARD-1", 2024L, 13))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusTransactionCardNumber req = monthReq("CARD-1", 2024L, 6);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findMonthTransactionStatusFailedByCard(any()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionMonthStatusFailed("2024", "Jun", 2, 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(2);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(200000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusTransactionCardNumber req = monthReq("CARD-1", 2024L, 6);
                        List<TransactionResponseMonthStatusFailed> cachedData = List.of(
                                        new TransactionResponseMonthStatusFailed("2024", "Jun", 200000L, 2L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusFailed tests")
        class FindYearlyStatusFailedTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCard_returnsError() {
                        ApiResponse<List<TransactionResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(yearReq(null, 2024L))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("should return error when year is null")
                void nullYear_returnsError() {
                        ApiResponse<List<TransactionResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(yearReq("CARD-1", null))
                                        .await().indefinitely();
                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearStatusTransactionCardNumber req = yearReq("CARD-1", 2024L);
                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(repository.findYearlyTransactionStatusFailedByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransactionYearStatusFailed("2024", 3, 300000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransactionResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(3);
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(300000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearStatusTransactionCardNumber req = yearReq("CARD-1", 2024L);
                        List<TransactionResponseYearStatusFailed> cachedData = List.of(
                                        new TransactionResponseYearStatusFailed("2024", 300000L, 3L));
                        when(redisService.getReactive("transactions:stats:status:card:year:failed:CARD-1:2024"))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransactionResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailed(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
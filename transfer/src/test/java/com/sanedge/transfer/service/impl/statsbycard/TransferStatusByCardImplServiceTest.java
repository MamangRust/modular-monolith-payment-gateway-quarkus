package com.sanedge.transfer.service.impl.statsbycard;

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
import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.transfer.domain.requests.statsbycard.YearStatusTransferCardNumber;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.transfer.entity.TransferMonthStatusFailed;
import com.sanedge.transfer.entity.TransferMonthStatusSuccess;
import com.sanedge.transfer.entity.TransferYearStatusFailed;
import com.sanedge.transfer.entity.TransferYearStatusSuccess;
import com.sanedge.transfer.repository.statsbycard.TransferStatusByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransferStatusByCardImplServiceTest {

        @Mock
        private TransferStatusByCardRepository transferStatusByCardRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransferStatusByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransferStatusByCardImplService(
                                transferStatusByCardRepository,
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

        private MonthStatusTransferCardNumber buildMonthReq(String cardNumber, int year, int month) {
                MonthStatusTransferCardNumber req = new MonthStatusTransferCardNumber();
                req.setCardNumber(cardNumber);
                req.setYear(year);
                req.setMonth(month);
                return req;
        }

        private YearStatusTransferCardNumber buildYearReq(String cardNumber, int year) {
                YearStatusTransferCardNumber req = new YearStatusTransferCardNumber();
                req.setCardNumber(cardNumber);
                req.setYear(year);
                return req;
        }

        @Nested
        @DisplayName("findMonthStatusSuccessByCard tests")
        class FindMonthStatusSuccessByCardTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCardNumber_returnsError() {
                        MonthStatusTransferCardNumber req = buildMonthReq(null, 2024, 6);
                        ApiResponse<List<TransferResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when month is invalid")
                void invalidMonth_returnsError() {
                        MonthStatusTransferCardNumber req = buildMonthReq("1234567890123456", 2024, 13);
                        ApiResponse<List<TransferResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusTransferCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferStatusByCardRepository.findMonthTransferStatusSuccessByCard(
                                        any(MonthStatusTransferCardNumber.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferMonthStatusSuccess("2024", "Jun", 5, 500000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(5);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusTransferCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

                        List<TransferResponseMonthStatusSuccess> cachedData = List.of(
                                        new TransferResponseMonthStatusSuccess("2024", "Jun", 500000L, 5L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferResponseMonthStatusSuccess>> result = service
                                        .findMonthStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusSuccessByCard tests")
        class FindYearlyStatusSuccessByCardTests {

                @Test
                @DisplayName("should return error when cardNumber is blank")
                void blankCardNumber_returnsError() {
                        YearStatusTransferCardNumber req = buildYearReq("   ", 2024);
                        ApiResponse<List<TransferResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearStatusTransferCardNumber req = buildYearReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferStatusByCardRepository.findYearlyTransferStatusSuccessByCard(anyString(),
                                        anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferYearStatusSuccess("2024", 10, 1000000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalSuccess()).isEqualTo(10);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearStatusTransferCardNumber req = buildYearReq("1234567890123456", 2024);

                        List<TransferResponseYearStatusSuccess> cachedData = List.of(
                                        new TransferResponseYearStatusSuccess("2024", 1000000L, 10L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferResponseYearStatusSuccess>> result = service
                                        .findYearlyStatusSuccessByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findMonthStatusFailedByCard tests")
        class FindMonthStatusFailedByCardTests {

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthStatusTransferCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferStatusByCardRepository.findMonthTransferStatusFailedByCard(
                                        any(MonthStatusTransferCardNumber.class)))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferMonthStatusFailed("2024", "Jun", 2, 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailedByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(2);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthStatusTransferCardNumber req = buildMonthReq("1234567890123456", 2024, 6);

                        List<TransferResponseMonthStatusFailed> cachedData = List.of(
                                        new TransferResponseMonthStatusFailed("2024", "Jun", 200000L, 2L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferResponseMonthStatusFailed>> result = service
                                        .findMonthStatusFailedByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyStatusFailedByCard tests")
        class FindYearlyStatusFailedByCardTests {

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearStatusTransferCardNumber req = buildYearReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferStatusByCardRepository.findYearlyTransferStatusFailedByCard(anyString(),
                                        anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferYearStatusFailed("2024", 3, 300000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailedByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getTotalFailed()).isEqualTo(3);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearStatusTransferCardNumber req = buildYearReq("1234567890123456", 2024);

                        List<TransferResponseYearStatusFailed> cachedData = List.of(
                                        new TransferResponseYearStatusFailed("2024", 300000L, 3L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferResponseYearStatusFailed>> result = service
                                        .findYearlyStatusFailedByCard(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
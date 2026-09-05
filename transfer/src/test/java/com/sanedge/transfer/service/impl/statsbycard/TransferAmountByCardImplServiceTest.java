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
import com.sanedge.transfer.domain.requests.statsbycard.MonthYearCardNumber;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.entity.TransferMonthAmount;
import com.sanedge.transfer.entity.TransferYearAmount;
import com.sanedge.transfer.repository.statsbycard.TransferAmountByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransferAmountByCardImplServiceTest {

        @Mock
        private TransferAmountByCardRepository transferAmountByCardRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private TransferAmountByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new TransferAmountByCardImplService(
                                transferAmountByCardRepository,
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

        private MonthYearCardNumber buildReq(String cardNumber, int year) {
                MonthYearCardNumber req = new MonthYearCardNumber();
                req.setCardNumber(cardNumber);
                req.setYear(year);
                return req;
        }

        @Nested
        @DisplayName("findMonthlyAmountsBySender tests")
        class FindMonthlyAmountsBySenderTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCardNumber_returnsError() {
                        MonthYearCardNumber req = buildReq(null, 2024);
                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when cardNumber is blank")
                void blankCardNumber_returnsError() {
                        MonthYearCardNumber req = buildReq("   ", 2024);
                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferAmountByCardRepository.findMonthlyTransferAmountsBySender(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferMonthAmount("Jan", 100000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        List<TransferMonthAmountResponse> cachedData = List.of(
                                        new TransferMonthAmountResponse("Jan", 100000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferMonthAmountResponse>> result = service.findMonthlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }

        @Nested
        @DisplayName("findYearlyAmountsBySender tests")
        class FindYearlyAmountsBySenderTests {

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferAmountByCardRepository.findYearlyTransferAmountsBySender(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferYearAmount("2024", 1200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getReportYear()).isEqualTo("2024");
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        List<TransferYearAmountResponse> cachedData = List.of(
                                        new TransferYearAmountResponse("2024", 1200000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmountsBySender(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                }
        }

        @Nested
        @DisplayName("findMonthlyAmountsByReceiver tests")
        class FindMonthlyAmountsByReceiverTests {

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(transferAmountByCardRepository.findMonthlyTransferAmountsByReceiver(anyString(),
                                        anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new TransferMonthAmount("Feb", 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<TransferMonthAmountResponse>> result = service
                                        .findMonthlyAmountsByReceiver(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Feb");
                }
        }

        @Nested
        @DisplayName("findYearlyAmountsByReceiver tests")
        class FindYearlyAmountsByReceiverTests {

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        MonthYearCardNumber req = buildReq("1234567890123456", 2024);

                        List<TransferYearAmountResponse> cachedData = List.of(
                                        new TransferYearAmountResponse("2024", 500000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<TransferYearAmountResponse>> result = service.findYearlyAmountsByReceiver(req)
                                        .await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                }
        }
}
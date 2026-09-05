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
import com.sanedge.withdraw.domain.requests.statsbycard.YearMonthCardNumber;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.entity.WithdrawMonthlyAmount;
import com.sanedge.withdraw.entity.WithdrawYearlyAmount;
import com.sanedge.withdraw.repository.statsbycard.WithdrawAmountByCardRepository;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class WithdrawAmountByCardImplServiceTest {

        @Mock
        private WithdrawAmountByCardRepository withdrawAmountByCardRepository;

        @Mock
        private RedisService redisService;

        @Mock
        private TracingMetrics tracingMetrics;

        private WithdrawAmountByCardImplService service;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() {
                service = new WithdrawAmountByCardImplService(
                                withdrawAmountByCardRepository,
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

        private YearMonthCardNumber buildReq(String cardNumber, long year) {
                YearMonthCardNumber req = new YearMonthCardNumber();
                req.setCardNumber(cardNumber);
                req.setYear((int) year);
                return req;
        }

        @Nested
        @DisplayName("findMonthlyByCardNumber tests")
        class FindMonthlyByCardNumberTests {

                @Test
                @DisplayName("should return error when year is invalid")
                void invalidYear_returnsError() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 0);
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCardNumber_returnsError() {
                        YearMonthCardNumber req = buildReq(null, 2024);
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("should return error when cardNumber is blank")
                void blankCardNumber_returnsError() {
                        YearMonthCardNumber req = buildReq("   ", 2024);
                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawAmountByCardRepository.findMonthlyWithdrawsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawMonthlyAmount("Jan", 100000L),
                                                        new WithdrawMonthlyAmount("Feb", 200000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
                        assertThat(result.data().get(0).getTotalAmount()).isEqualTo(100000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        List<WithdrawMonthlyAmountResponse> cachedData = List.of(
                                        new WithdrawMonthlyAmountResponse("Jan", 100000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getMonthName()).isEqualTo("Jan");
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

                        ApiResponse<List<WithdrawMonthlyAmountResponse>> result = service
                                        .findMonthlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to fetch monthly withdraw amounts by card");
                }
        }

        @Nested
        @DisplayName("findYearlyByCardNumber tests")
        class FindYearlyByCardNumberTests {

                @Test
                @DisplayName("should return error when cardNumber is null")
                void nullCardNumber_returnsError() {
                        YearMonthCardNumber req = buildReq(null, 2024);
                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service
                                        .findYearlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.data()).isEmpty();
                }

                @Test
                @DisplayName("cache miss - should fetch from DB and cache result")
                void cacheMiss_fetchesFromDb() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
                        when(withdrawAmountByCardRepository.findYearlyWithdrawsByCard(anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().item(List.of(
                                                        new WithdrawYearlyAmount("2023", 500000L),
                                                        new WithdrawYearlyAmount("2024", 700000L))));
                        when(redisService.setWithExpirationReactive(anyString(), anyString(), anyLong()))
                                        .thenReturn(Uni.createFrom().voidItem());

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service
                                        .findYearlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(2);
                        assertThat(result.data().get(1).getReportYear()).isEqualTo("2024");
                        assertThat(result.data().get(1).getTotalAmount()).isEqualTo(700000L);
                }

                @Test
                @DisplayName("cache hit - should return cached response without DB call")
                void cacheHit_returnsCached() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        List<WithdrawYearlyAmountResponse> cachedData = List.of(
                                        new WithdrawYearlyAmountResponse("2024", 700000L));
                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().item(toJson(cachedData)));

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service
                                        .findYearlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("success");
                        assertThat(result.data()).hasSize(1);
                        assertThat(result.data().get(0).getReportYear()).isEqualTo("2024");
                }

                @Test
                @DisplayName("on failure - should return error response")
                void onFailure_returnsErrorResponse() {
                        YearMonthCardNumber req = buildReq("1234567890123456", 2024);

                        when(redisService.getReactive(anyString()))
                                        .thenReturn(Uni.createFrom().failure(new RuntimeException("Redis down")));

                        ApiResponse<List<WithdrawYearlyAmountResponse>> result = service
                                        .findYearlyByCardNumber(req).await().indefinitely();

                        assertThat(result.status()).isEqualTo("error");
                        assertThat(result.message()).isEqualTo("Failed to fetch yearly withdraw amounts by card");
                }
        }
}
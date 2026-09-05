package com.sanedge.card.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;
import com.sanedge.card.service.stats.CardBalanceService;
import com.sanedge.card.service.statsbycard.CardBalanceByCardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.stats.CardStatsBalance;

@ExtendWith(MockitoExtension.class)
class CardStatsBalanceGrpcHandlerTest {

        @Mock
        private CardBalanceService cardBalanceService;
        @Mock
        private CardBalanceByCardService cardBalanceByCardService;

        private CardStatsBalanceGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new CardStatsBalanceGrpcHandler();
                handler.cardBalanceService = cardBalanceService;
                handler.cardBalanceByCardService = cardBalanceByCardService;
        }

        // helpers
        private CardResponseMonthBalance monthBalance(String month, long balance) {
                CardResponseMonthBalance mb = new CardResponseMonthBalance();
                mb.setMonth(month);
                mb.setTotalBalance(balance);
                return mb;
        }

        private CardResponseYearBalance yearBalance(String year, long balance) {
                CardResponseYearBalance yb = new CardResponseYearBalance();
                yb.setReportYear(year);
                yb.setTotalBalance(balance);
                return yb;
        }

        @Test
        @DisplayName("findMonthlyBalance - success")
        void findMonthly_Success() {
                CardStatsBalance.FindYearBalance request = CardStatsBalance.FindYearBalance.newBuilder().setYear(2024)
                                .build();
                List<CardResponseMonthBalance> data = List.of(monthBalance("Jan", 1000L));
                ApiResponse<List<CardResponseMonthBalance>> apiResp = ApiResponse.success("Success", data);
                when(cardBalanceService.findMonthBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                CardStatsBalance.ApiResponseMonthlyBalance response = handler.findMonthlyBalance(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("findMonthlyBalance - error")
        void findMonthly_Error() {
                CardStatsBalance.FindYearBalance request = CardStatsBalance.FindYearBalance.newBuilder().setYear(2024)
                                .build();
                when(cardBalanceService.findMonthBalance(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyBalance(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyBalance - success")
        void findYearly_Success() {
                CardStatsBalance.FindYearBalance request = CardStatsBalance.FindYearBalance.newBuilder().setYear(2024)
                                .build();
                List<CardResponseYearBalance> data = List.of(yearBalance("2024", 5000L));
                ApiResponse<List<CardResponseYearBalance>> apiResp = ApiResponse.success("Success", data);
                when(cardBalanceService.findYearBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                CardStatsBalance.ApiResponseYearlyBalance response = handler.findYearlyBalance(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("findYearlyBalance - error")
        void findYearly_Error() {
                CardStatsBalance.FindYearBalance request = CardStatsBalance.FindYearBalance.newBuilder().setYear(2024)
                                .build();
                when(cardBalanceService.findYearBalance(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyBalance(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findMonthlyBalanceByCardNumber - success")
        void findMonthlyByCard_Success() {
                CardStatsBalance.FindYearBalanceCardNumber request = CardStatsBalance.FindYearBalanceCardNumber
                                .newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthBalance> data = List.of(monthBalance("Feb", 2000L));
                ApiResponse<List<CardResponseMonthBalance>> apiResp = ApiResponse.success("Success", data);
                when(cardBalanceByCardService.findMonthBalanceByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                CardStatsBalance.ApiResponseMonthlyBalance response = handler.findMonthlyBalanceByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Feb");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("findMonthlyBalanceByCardNumber - error")
        void findMonthlyByCard_Error() {
                CardStatsBalance.FindYearBalanceCardNumber request = CardStatsBalance.FindYearBalanceCardNumber
                                .newBuilder().build();
                when(cardBalanceByCardService.findMonthBalanceByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyBalanceByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyBalanceByCardNumber - success")
        void findYearlyByCard_Success() {
                CardStatsBalance.FindYearBalanceCardNumber request = CardStatsBalance.FindYearBalanceCardNumber
                                .newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearBalance> data = List.of(yearBalance("2024", 6000L));
                ApiResponse<List<CardResponseYearBalance>> apiResp = ApiResponse.success("Success", data);
                when(cardBalanceByCardService.findYearBalanceByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                CardStatsBalance.ApiResponseYearlyBalance response = handler.findYearlyBalanceByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("findYearlyBalanceByCardNumber - error")
        void findYearlyByCard_Error() {
                CardStatsBalance.FindYearBalanceCardNumber request = CardStatsBalance.FindYearBalanceCardNumber
                                .newBuilder().build();
                when(cardBalanceByCardService.findYearBalanceByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findYearlyBalanceByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // edge case: null data
        @Test
        @DisplayName("findMonthlyBalance - null data")
        void findMonthly_NullData() {
                CardStatsBalance.FindYearBalance request = CardStatsBalance.FindYearBalance.newBuilder().setYear(2024)
                                .build();
                ApiResponse<List<CardResponseMonthBalance>> apiResp = ApiResponse.success("Success", null);
                when(cardBalanceService.findMonthBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                CardStatsBalance.ApiResponseMonthlyBalance response = handler.findMonthlyBalance(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isZero();
        }
}
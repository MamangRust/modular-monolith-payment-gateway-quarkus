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

import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;
import com.sanedge.card.service.stats.CardWithdrawAmountService;
import com.sanedge.card.service.statsbycard.CardWithdrawAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.Card;

@ExtendWith(MockitoExtension.class)
class CardStatsWithdrawGrpcHandlerTest {

        @Mock
        private CardWithdrawAmountService withdrawService;
        @Mock
        private CardWithdrawAmountByCardService withdrawByCardService;

        private CardStatsWithdrawGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new CardStatsWithdrawGrpcHandler();
                handler.cardWithdrawAmountService = withdrawService;
                handler.cardWithdrawAmountByCardService = withdrawByCardService;
        }

        private CardResponseMonthAmount monthAmount(String month, long amount) {
                CardResponseMonthAmount m = new CardResponseMonthAmount();
                m.setMonthName(month);
                m.setTotalAmount(amount);
                return m;
        }

        private CardResponseYearAmount yearAmount(String year, long amount) {
                CardResponseYearAmount y = new CardResponseYearAmount();
                y.setReportYear(year);
                y.setTotalAmount(amount);
                return y;
        }

        @Test
        @DisplayName("findMonthlyWithdrawAmount - success")
        void findMonthly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Jun", 5000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(withdrawService.findMonthAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyWithdrawAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("findMonthlyWithdrawAmount - error")
        void findMonthly_Error() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().build();
                when(withdrawService.findMonthAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyWithdrawAmount(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyWithdrawAmount - success")
        void findYearly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 9000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(withdrawService.findYearAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyWithdrawAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(9000L);
        }

        @Test
        @DisplayName("findMonthlyWithdrawAmountByCardNumber - success")
        void findMonthlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Jul", 10000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(withdrawByCardService.findMonthAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyWithdrawAmountByCardNumber(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Jul");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("findYearlyWithdrawAmountByCardNumber - success")
        void findYearlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 12000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(withdrawByCardService.findYearAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyWithdrawAmountByCardNumber(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(12000L);
        }
}
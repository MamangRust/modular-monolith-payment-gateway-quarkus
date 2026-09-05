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
import com.sanedge.card.service.stats.CardTransactionAmountService;
import com.sanedge.card.service.statsbycard.CardTransactionAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.Card;

@ExtendWith(MockitoExtension.class)
class CardStatsTransactionGrpcHandlerTest {

        @Mock
        private CardTransactionAmountService txService;
        @Mock
        private CardTransactionAmountByCardService txByCardService;

        private CardStatsTransactionGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new CardStatsTransactionGrpcHandler();
                handler.cardTransactionAmountService = txService;
                handler.cardTransactionAmountByCardService = txByCardService;
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
        @DisplayName("findMonthlyTransactionAmount - success")
        void findMonthly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Mar", 4000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(txService.findMonthAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransactionAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Mar");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(4000L);
        }

        @Test
        @DisplayName("findMonthlyTransactionAmount - error")
        void findMonthly_Error() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().build();
                when(txService.findMonthAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyTransactionAmount(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTransactionAmount - success")
        void findYearly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 8000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(txService.findYearAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransactionAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(8000L);
        }

        @Test
        @DisplayName("findMonthlyTransactionAmountByCardNumber - success")
        void findMonthlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Apr", 5000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(txByCardService.findMonthAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransactionAmountByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Apr");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("findYearlyTransactionAmountByCardNumber - success")
        void findYearlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 9000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(txByCardService.findYearAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransactionAmountByCardNumber(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(9000L);
        }
}
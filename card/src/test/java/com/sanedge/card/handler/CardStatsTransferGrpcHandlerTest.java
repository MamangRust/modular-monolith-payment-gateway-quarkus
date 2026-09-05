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
import com.sanedge.card.service.stats.CardTransferAmountService;
import com.sanedge.card.service.statsbycard.CardTransferAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.Card;

@ExtendWith(MockitoExtension.class)
class CardStatsTransferGrpcHandlerTest {

        @Mock
        private CardTransferAmountService transferService;
        @Mock
        private CardTransferAmountByCardService transferByCardService;

        private CardStatsTransferGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new CardStatsTransferGrpcHandler();
                handler.cardTransferAmountService = transferService;
                handler.cardTransferAmountByCardService = transferByCardService;
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

        // Sender – monthly
        @Test
        @DisplayName("findMonthlyTransferSenderAmount - success")
        void findMonthlySender_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("May", 6000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferService.findMonthAmountSender(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransferSenderAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("May");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("findMonthlyTransferSenderAmount - error")
        void findMonthlySender_Error() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().build();
                when(transferService.findMonthAmountSender(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyTransferSenderAmount(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // Sender – yearly
        @Test
        @DisplayName("findYearlyTransferSenderAmount - success")
        void findYearlySender_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 12000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferService.findYearAmountSender(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransferSenderAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(12000L);
        }

        // Receiver – monthly
        @Test
        @DisplayName("findMonthlyTransferReceiverAmount - success")
        void findMonthlyReceiver_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Jun", 7000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferService.findMonthAmountReceiver(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransferReceiverAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(7000L);
        }

        // Receiver – yearly
        @Test
        @DisplayName("findYearlyTransferReceiverAmount - success")
        void findYearlyReceiver_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 14000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferService.findYearAmountReceiver(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransferReceiverAmount(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(14000L);
        }

        // By card – sender monthly
        @Test
        @DisplayName("findMonthlyTransferSenderAmountByCardNumber - success")
        void findMonthlySenderByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Jul", 8000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferByCardService.findMonthAmountSender(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransferSenderAmountByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Jul");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(8000L);
        }

        // By card – sender yearly
        @Test
        @DisplayName("findYearlyTransferSenderAmountByCardNumber - success")
        void findYearlySenderByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 16000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferByCardService.findYearAmountSender(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransferSenderAmountByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(16000L);
        }

        // By card – receiver monthly
        @Test
        @DisplayName("findMonthlyTransferReceiverAmountByCardNumber - success")
        void findMonthlyReceiverByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Aug", 9000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferByCardService.findMonthAmountReceiver(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTransferReceiverAmountByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Aug");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(9000L);
        }

        // By card – receiver yearly
        @Test
        @DisplayName("findYearlyTransferReceiverAmountByCardNumber - success")
        void findYearlyReceiverByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 18000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(transferByCardService.findYearAmountReceiver(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTransferReceiverAmountByCardNumber(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(18000L);
        }
}
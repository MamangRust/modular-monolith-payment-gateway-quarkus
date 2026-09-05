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
import com.sanedge.card.service.stats.CardTopupAmountService;
import com.sanedge.card.service.statsbycard.CardTopupAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.Card;

@ExtendWith(MockitoExtension.class)
class CardStatsTopupGrpcHandlerTest {

        @Mock
        private CardTopupAmountService topupService;
        @Mock
        private CardTopupAmountByCardService topupByCardService;

        private CardStatsTopupGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new CardStatsTopupGrpcHandler();
                handler.cardTopupAmountService = topupService;
                handler.cardTopupAmountByCardService = topupByCardService;
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
        @DisplayName("findMonthlyTopupAmount - success")
        void findMonthly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Jan", 2000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(topupService.findMonthAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTopupAmount(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("findMonthlyTopupAmount - error")
        void findMonthly_Error() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().build();
                when(topupService.findMonthAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyTopupAmount(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTopupAmount - success")
        void findYearly_Success() {
                Card.FindYearAmount request = Card.FindYearAmount.newBuilder().setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 6000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(topupService.findYearAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTopupAmount(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("findMonthlyTopupAmountByCardNumber - success")
        void findMonthlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseMonthAmount> data = List.of(monthAmount("Feb", 3000L));
                ApiResponse<List<CardResponseMonthAmount>> apiResp = ApiResponse.success("Success", data);
                when(topupByCardService.findMonthAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseMonthlyAmount response = handler.findMonthlyTopupAmountByCardNumber(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Feb");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("findMonthlyTopupAmountByCardNumber - error")
        void findMonthlyByCard_Error() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder().build();
                when(topupByCardService.findMonthAmountByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
                try {
                        handler.findMonthlyTopupAmountByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTopupAmountByCardNumber - success")
        void findYearlyByCard_Success() {
                Card.FindYearAmountCardNumber request = Card.FindYearAmountCardNumber.newBuilder()
                                .setCardNumber("CARD-1").setYear(2024).build();
                List<CardResponseYearAmount> data = List.of(yearAmount("2024", 7000L));
                ApiResponse<List<CardResponseYearAmount>> apiResp = ApiResponse.success("Success", data);
                when(topupByCardService.findYearAmountByCard(any())).thenReturn(Uni.createFrom().item(apiResp));

                Card.ApiResponseYearlyAmount response = handler.findYearlyTopupAmountByCardNumber(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(7000L);
        }
}
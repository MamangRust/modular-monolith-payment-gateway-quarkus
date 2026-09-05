package com.sanedge.withdraw.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountByCardService;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.withdraw.Withdraw;
import pb.withdraw.stats.WithdrawStatsAmount;

@ExtendWith(MockitoExtension.class)
class WithdrawStatsAmountGrpcHandlerTest {

        @Mock
        private WithdrawAmountService withdrawAmountService;

        @Mock
        private WithdrawAmountByCardService withdrawAmountByCardService;

        private WithdrawStatsAmountGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new WithdrawStatsAmountGrpcHandler();
                handler.withdrawAmountService = withdrawAmountService;
                handler.withdrawAmountByCardService = withdrawAmountByCardService;
        }

        @Test
        @DisplayName("findMonthlyWithdraws - should return list on success")
        void findMonthlyWithdraws_Success() {
                Withdraw.FindYearWithdrawStatus request = Withdraw.FindYearWithdrawStatus.newBuilder()
                                .setYear(2024).build();

                WithdrawMonthlyAmountResponse data = new WithdrawMonthlyAmountResponse("Jan", 100000L);
                ApiResponse<List<WithdrawMonthlyAmountResponse>> apiResp = ApiResponse.success(
                                "Monthly withdraw amounts retrieved successfully", List.of(data));

                when(withdrawAmountService.findMonthlyWithdraws(any())).thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsAmount.ApiResponseWithdrawMonthAmount response = handler.findMonthlyWithdraws(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(100000);
        }

        @Test
        @DisplayName("findMonthlyWithdraws - should return INTERNAL on failure")
        void findMonthlyWithdraws_InternalError() {
                Withdraw.FindYearWithdrawStatus request = Withdraw.FindYearWithdrawStatus.newBuilder()
                                .setYear(2024).build();

                when(withdrawAmountService.findMonthlyWithdraws(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyWithdraws(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        @Test
        @DisplayName("findYearlyWithdraws - should return list on success")
        void findYearlyWithdraws_Success() {
                Withdraw.FindYearWithdrawStatus request = Withdraw.FindYearWithdrawStatus.newBuilder()
                                .setYear(2024).build();

                WithdrawYearlyAmountResponse data = new WithdrawYearlyAmountResponse("2024", 1200000L);
                ApiResponse<List<WithdrawYearlyAmountResponse>> apiResp = ApiResponse.success(
                                "Yearly withdraw amounts retrieved successfully", List.of(data));

                when(withdrawAmountService.findYearlyWithdraws(any())).thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsAmount.ApiResponseWithdrawYearAmount response = handler.findYearlyWithdraws(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1200000);
        }

        @Test
        @DisplayName("findMonthlyWithdrawsByCardNumber - should return list on success")
        void findMonthlyWithdrawsByCardNumber_Success() {
                Withdraw.FindYearWithdrawCardNumber request = Withdraw.FindYearWithdrawCardNumber.newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).build();

                WithdrawMonthlyAmountResponse data = new WithdrawMonthlyAmountResponse("Jan", 100000L);
                ApiResponse<List<WithdrawMonthlyAmountResponse>> apiResp = ApiResponse.success(
                                "Monthly withdraw amounts by card retrieved successfully", List.of(data));

                when(withdrawAmountByCardService.findMonthlyByCardNumber(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsAmount.ApiResponseWithdrawMonthAmount response = handler
                                .findMonthlyWithdrawsByCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findYearlyWithdrawsByCardNumber - should return list on success")
        void findYearlyWithdrawsByCardNumber_Success() {
                Withdraw.FindYearWithdrawCardNumber request = Withdraw.FindYearWithdrawCardNumber.newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).build();

                WithdrawYearlyAmountResponse data = new WithdrawYearlyAmountResponse("2024", 1200000L);
                ApiResponse<List<WithdrawYearlyAmountResponse>> apiResp = ApiResponse.success(
                                "Yearly withdraw amounts by card retrieved successfully", List.of(data));

                when(withdrawAmountByCardService.findYearlyByCardNumber(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsAmount.ApiResponseWithdrawYearAmount response = handler
                                .findYearlyWithdrawsByCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }
}
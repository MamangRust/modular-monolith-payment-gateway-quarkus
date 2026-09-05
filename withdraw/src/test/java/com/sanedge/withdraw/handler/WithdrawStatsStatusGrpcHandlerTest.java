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
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusByCardService;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.withdraw.Withdraw;
import pb.withdraw.stats.WithdrawStatsStatus;

@ExtendWith(MockitoExtension.class)
class WithdrawStatsStatusGrpcHandlerTest {

        @Mock
        private WithdrawStatusService withdrawStatusService;

        @Mock
        private WithdrawStatusByCardService withdrawStatusByCardService;

        private WithdrawStatsStatusGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new WithdrawStatsStatusGrpcHandler();
                handler.withdrawStatusService = withdrawStatusService;
                handler.withdrawStatusByCardService = withdrawStatusByCardService;
        }

        @Test
        @DisplayName("findMonthlyWithdrawStatusSuccess - should return list on success")
        void findMonthlyWithdrawStatusSuccess_Success() {
                Withdraw.FindMonthlyWithdrawStatus request = Withdraw.FindMonthlyWithdrawStatus.newBuilder()
                                .setYear(2024).setMonth(6).build();

                WithdrawResponseMonthStatusSuccess data = new WithdrawResponseMonthStatusSuccess("2024", "Jun", 500000L,
                                5L);
                ApiResponse<List<WithdrawResponseMonthStatusSuccess>> apiResp = ApiResponse.success(
                                "Monthly SUCCESS withdraw status fetched successfully", List.of(data));

                when(withdrawStatusService.findMonthStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess response = handler
                                .findMonthlyWithdrawStatusSuccess(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(5);
        }

        @Test
        @DisplayName("findMonthlyWithdrawStatusSuccess - should return INTERNAL on failure")
        void findMonthlyWithdrawStatusSuccess_InternalError() {
                Withdraw.FindMonthlyWithdrawStatus request = Withdraw.FindMonthlyWithdrawStatus.newBuilder()
                                .setYear(2024).setMonth(6).build();

                when(withdrawStatusService.findMonthStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyWithdrawStatusSuccess(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        @Test
        @DisplayName("findYearlyWithdrawStatusSuccess - should return list on success")
        void findYearlyWithdrawStatusSuccess_Success() {
                Withdraw.FindYearWithdrawStatus request = Withdraw.FindYearWithdrawStatus.newBuilder()
                                .setYear(2024).build();

                WithdrawResponseYearStatusSuccess data = new WithdrawResponseYearStatusSuccess("2024", 1000000L,
                                10L);
                ApiResponse<List<WithdrawResponseYearStatusSuccess>> apiResp = ApiResponse.success(
                                "Yearly SUCCESS withdraw status fetched successfully", List.of(data));

                when(withdrawStatusService.findYearlyStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawYearStatusSuccess response = handler
                                .findYearlyWithdrawStatusSuccess(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(10);
        }

        @Test
        @DisplayName("findMonthlyWithdrawStatusFailed - should return list on success")
        void findMonthlyWithdrawStatusFailed_Success() {
                Withdraw.FindMonthlyWithdrawStatus request = Withdraw.FindMonthlyWithdrawStatus.newBuilder()
                                .setYear(2024).setMonth(6).build();

                WithdrawResponseMonthStatusFailed data = new WithdrawResponseMonthStatusFailed("2024", "Jun", 200000L,
                                2L);
                ApiResponse<List<WithdrawResponseMonthStatusFailed>> apiResp = ApiResponse.success(
                                "Monthly FAILED withdraw status fetched successfully", List.of(data));

                when(withdrawStatusService.findMonthStatusFailed(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawMonthStatusFailed response = handler
                                .findMonthlyWithdrawStatusFailed(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(2);
        }

        @Test
        @DisplayName("findYearlyWithdrawStatusFailed - should return list on success")
        void findYearlyWithdrawStatusFailed_Success() {
                Withdraw.FindYearWithdrawStatus request = Withdraw.FindYearWithdrawStatus.newBuilder()
                                .setYear(2024).build();

                WithdrawResponseYearStatusFailed data = new WithdrawResponseYearStatusFailed("2024", 300000L, 3L);
                ApiResponse<List<WithdrawResponseYearStatusFailed>> apiResp = ApiResponse.success(
                                "Yearly FAILED withdraw status fetched successfully", List.of(data));

                when(withdrawStatusService.findYearlyStatusFailed(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawYearStatusFailed response = handler
                                .findYearlyWithdrawStatusFailed(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findMonthlyWithdrawStatusSuccessCardNumber - should return list on success")
        void findMonthlyWithdrawStatusSuccessCardNumber_Success() {
                Withdraw.FindMonthlyWithdrawStatusCardNumber request = Withdraw.FindMonthlyWithdrawStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).setMonth(6).build();

                WithdrawResponseMonthStatusSuccess data = new WithdrawResponseMonthStatusSuccess("2024", "Jun", 500000L,
                                5L);
                ApiResponse<List<WithdrawResponseMonthStatusSuccess>> apiResp = ApiResponse.success(
                                "Monthly SUCCESS withdraw status by card fetched successfully", List.of(data));

                when(withdrawStatusByCardService.findMonthStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess response = handler
                                .findMonthlyWithdrawStatusSuccessCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findYearlyWithdrawStatusSuccessCardNumber - should return list on success")
        void findYearlyWithdrawStatusSuccessCardNumber_Success() {
                Withdraw.FindYearWithdrawStatusCardNumber request = Withdraw.FindYearWithdrawStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).build();

                WithdrawResponseYearStatusSuccess data = new WithdrawResponseYearStatusSuccess("2024", 1000000L,
                                10L);
                ApiResponse<List<WithdrawResponseYearStatusSuccess>> apiResp = ApiResponse.success(
                                "Yearly SUCCESS withdraw status by card fetched successfully", List.of(data));

                when(withdrawStatusByCardService.findYearlyStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawYearStatusSuccess response = handler
                                .findYearlyWithdrawStatusSuccessCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findMonthlyWithdrawStatusFailedCardNumber - should return list on success")
        void findMonthlyWithdrawStatusFailedCardNumber_Success() {
                Withdraw.FindMonthlyWithdrawStatusCardNumber request = Withdraw.FindMonthlyWithdrawStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).setMonth(6).build();

                WithdrawResponseMonthStatusFailed data = new WithdrawResponseMonthStatusFailed("2024", "Jun", 200000L,
                                2l);
                ApiResponse<List<WithdrawResponseMonthStatusFailed>> apiResp = ApiResponse.success(
                                "Monthly FAILED withdraw status by card fetched successfully", List.of(data));

                when(withdrawStatusByCardService.findMonthStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawMonthStatusFailed response = handler
                                .findMonthlyWithdrawStatusFailedCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findYearlyWithdrawStatusFailedCardNumber - should return list on success")
        void findYearlyWithdrawStatusFailedCardNumber_Success() {
                Withdraw.FindYearWithdrawStatusCardNumber request = Withdraw.FindYearWithdrawStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456").setYear(2024).build();

                WithdrawResponseYearStatusFailed data = new WithdrawResponseYearStatusFailed("2024", 300000L, 3l);
                ApiResponse<List<WithdrawResponseYearStatusFailed>> apiResp = ApiResponse.success(
                                "Yearly FAILED withdraw status by card fetched successfully", List.of(data));

                when(withdrawStatusByCardService.findYearlyStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                WithdrawStatsStatus.ApiResponseWithdrawYearStatusFailed response = handler
                                .findYearlyWithdrawStatusFailedCardNumber(request).await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }
}
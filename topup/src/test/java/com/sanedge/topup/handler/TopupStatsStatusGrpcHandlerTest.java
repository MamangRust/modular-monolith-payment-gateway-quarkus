package com.sanedge.topup.handler;

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

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.topup.service.stats.status.TopupStatusByCardService;
import com.sanedge.topup.service.stats.status.TopupStatusService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.topup.stats.TopupStatsStatus;

@ExtendWith(MockitoExtension.class)
class TopupStatsStatusGrpcHandlerTest {

        @Mock
        private TopupStatusService topupStatusService;

        @Mock
        private TopupStatusByCardService topupStatusByCardService;

        private TopupStatsStatusGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new TopupStatsStatusGrpcHandler();
                handler.topupStatusService = topupStatusService;
                handler.topupStatusByCardService = topupStatusByCardService;
        }

        // helpers
        private TopupResponseMonthStatusSuccess monthSuccess(String year, String month, long amount, int count) {
                return new TopupResponseMonthStatusSuccess(year, month, amount, (long) count);
        }

        private TopupResponseMonthStatusFailed monthFailed(String year, String month, long amount, int count) {
                return new TopupResponseMonthStatusFailed(year, month, amount, (long) count);
        }

        private TopupResponseYearStatusSuccess yearSuccess(String year, long amount, int count) {
                return new TopupResponseYearStatusSuccess(year, amount, (long) count);
        }

        private TopupResponseYearStatusFailed yearFailed(String year, long amount, int count) {
                return new TopupResponseYearStatusFailed(year, amount, (long) count);
        }

        @Test
        @DisplayName("findMonthlyStatusSuccess - success")
        void findMonthlySuccess_Success() {
                pb.topup.Topup.FindMonthlyTopupStatus request = pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                                .setYear(2024).setMonth(6).build();

                List<TopupResponseMonthStatusSuccess> data = List.of(monthSuccess("2024", "Jun", 50000L, 5));
                ApiResponse<List<TopupResponseMonthStatusSuccess>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusService.findMonthStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupMonthStatusSuccess response = handler
                                .findMonthlyTopupStatusSuccess(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(5);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(50000);
        }

        @Test
        @DisplayName("findMonthlyStatusSuccess - error")
        void findMonthlySuccess_Error() {
                pb.topup.Topup.FindMonthlyTopupStatus request = pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                                .build();
                when(topupStatusService.findMonthStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTopupStatusSuccess(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyStatusSuccess - success")
        void findYearlySuccess_Success() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();
                List<TopupResponseYearStatusSuccess> data = List.of(yearSuccess("2024", 100000L, 10));
                ApiResponse<List<TopupResponseYearStatusSuccess>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusService.findYearlyStatusSuccess(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupYearStatusSuccess response = handler
                                .findYearlyTopupStatusSuccess(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(10);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(100000);
        }

        @Test
        @DisplayName("findYearlyStatusSuccess - error")
        void findYearlySuccess_Error() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder().build();
                when(topupStatusService.findYearlyStatusSuccess(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyTopupStatusSuccess(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findMonthlyStatusFailed - success")
        void findMonthlyFailed_Success() {
                pb.topup.Topup.FindMonthlyTopupStatus request = pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                                .setYear(2024).setMonth(6).build();
                List<TopupResponseMonthStatusFailed> data = List.of(monthFailed("2024", "Jun", 20000L, 2));
                ApiResponse<List<TopupResponseMonthStatusFailed>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusService.findMonthStatusFailed(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupMonthStatusFailed response = handler
                                .findMonthlyTopupStatusFailed(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(2);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(20000);
        }

        @Test
        @DisplayName("findMonthlyStatusFailed - error")
        void findMonthlyFailed_Error() {
                pb.topup.Topup.FindMonthlyTopupStatus request = pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                                .build();
                when(topupStatusService.findMonthStatusFailed(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTopupStatusFailed(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findMonthlyStatusSuccessByCard - success")
        void findMonthlySuccessByCard_Success() {
                pb.topup.Topup.FindMonthlyTopupStatusCardNumber request = pb.topup.Topup.FindMonthlyTopupStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234").setYear(2024).setMonth(6).build();

                List<TopupResponseMonthStatusSuccess> data = List.of(monthSuccess("2024", "Jun", 40000L, 4));
                ApiResponse<List<TopupResponseMonthStatusSuccess>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusByCardService.findMonthStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupMonthStatusSuccess response = handler
                                .findMonthlyTopupStatusSuccessByCardNumber(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(4);
        }

        @Test
        @DisplayName("findMonthlyStatusSuccessByCard - error")
        void findMonthlySuccessByCard_Error() {
                pb.topup.Topup.FindMonthlyTopupStatusCardNumber request = pb.topup.Topup.FindMonthlyTopupStatusCardNumber
                                .newBuilder().build();
                when(topupStatusByCardService.findMonthStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
                try {
                        handler.findMonthlyTopupStatusSuccessByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyStatusSuccessByCard - success")
        void findYearlySuccessByCard_Success() {
                pb.topup.Topup.FindYearTopupStatusCardNumber request = pb.topup.Topup.FindYearTopupStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234").setYear(2024).build();

                List<TopupResponseYearStatusSuccess> data = List.of(yearSuccess("2024", 80000L, 8));
                ApiResponse<List<TopupResponseYearStatusSuccess>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusByCardService.findYearlyStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupYearStatusSuccess response = handler
                                .findYearlyTopupStatusSuccessByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("findMonthlyStatusFailedByCard - success")
        void findMonthlyFailedByCard_Success() {
                pb.topup.Topup.FindMonthlyTopupStatusCardNumber request = pb.topup.Topup.FindMonthlyTopupStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234").setYear(2024).setMonth(7).build();

                List<TopupResponseMonthStatusFailed> data = List.of(monthFailed("2024", "Jul", 15000L, 1));
                ApiResponse<List<TopupResponseMonthStatusFailed>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusByCardService.findMonthStatusFailed(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupMonthStatusFailed response = handler
                                .findMonthlyTopupStatusFailedByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(1);
        }

        @Test
        @DisplayName("findYearlyStatusFailedByCard - success")
        void findYearlyFailedByCard_Success() {
                pb.topup.Topup.FindYearTopupStatusCardNumber request = pb.topup.Topup.FindYearTopupStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234").setYear(2024).build();

                List<TopupResponseYearStatusFailed> data = List.of(yearFailed("2024", 30000L, 3));
                ApiResponse<List<TopupResponseYearStatusFailed>> apiResp = ApiResponse.success("Success", data);
                when(topupStatusByCardService.findYearlyStatusFailed(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsStatus.ApiResponseTopupYearStatusFailed response = handler
                                .findYearlyTopupStatusFailedByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(3);
        }

        @Test
        @DisplayName("findYearlyStatusFailedByCard - error")
        void findYearlyFailedByCard_Error() {
                pb.topup.Topup.FindYearTopupStatusCardNumber request = pb.topup.Topup.FindYearTopupStatusCardNumber
                                .newBuilder().build();
                when(topupStatusByCardService.findYearlyStatusFailed(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
                try {
                        handler.findYearlyTopupStatusFailedByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }
}
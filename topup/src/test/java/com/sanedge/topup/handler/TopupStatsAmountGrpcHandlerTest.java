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
import com.sanedge.topup.domain.response.stats.amount.TopupMonthAmountResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupYearlyAmountResponse;
import com.sanedge.topup.service.stats.amount.TopupAmountByCardService;
import com.sanedge.topup.service.stats.amount.TopupAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.topup.stats.TopupStatsAmount;

@ExtendWith(MockitoExtension.class)
class TopupStatsAmountGrpcHandlerTest {

        @Mock
        private TopupAmountService topupAmountService;

        @Mock
        private TopupAmountByCardService topupAmountByCardService;

        private TopupStatsAmountGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new TopupStatsAmountGrpcHandler();
                handler.topupAmountService = topupAmountService;
                handler.topupAmountByCardService = topupAmountByCardService;
        }

        // ---------- helpers ----------
        private TopupMonthAmountResponse monthAmount(String month, long amount) {
                return new TopupMonthAmountResponse(month, amount);
        }

        private TopupYearlyAmountResponse yearlyAmount(String year, long amount) {
                return new TopupYearlyAmountResponse(year, amount);
        }

        // ========== findMonthlyTopupAmounts ==========
        @Test
        @DisplayName("findMonthlyTopupAmounts - success with data")
        void findMonthly_Success() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();

                List<TopupMonthAmountResponse> data = List.of(monthAmount("Jan", 1000L));
                ApiResponse<List<TopupMonthAmountResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupAmountService.findMonthlyAmounts(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsAmount.ApiResponseTopupMonthAmount response = handler.findMonthlyTopupAmounts(request).await()
                                .indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1000);
        }

        @Test
        @DisplayName("findMonthlyTopupAmounts - internal error")
        void findMonthly_Error() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();
                when(topupAmountService.findMonthlyAmounts(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTopupAmounts(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findYearlyTopupAmounts ==========
        @Test
        @DisplayName("findYearlyTopupAmounts - success with data")
        void findYearly_Success() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();

                List<TopupYearlyAmountResponse> data = List.of(yearlyAmount("2024", 5000L));
                ApiResponse<List<TopupYearlyAmountResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupAmountService.findYearlyAmounts(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsAmount.ApiResponseTopupYearAmount response = handler.findYearlyTopupAmounts(request).await()
                                .indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(5000);
        }

        @Test
        @DisplayName("findYearlyTopupAmounts - internal error")
        void findYearly_Error() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder().build();
                when(topupAmountService.findYearlyAmounts(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyTopupAmounts(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findMonthlyTopupAmountsByCardNumber ==========
        @Test
        @DisplayName("findMonthlyByCard - success")
        void findMonthlyByCard_Success() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .setCardNumber("1234").setYear(2024).build();

                List<TopupMonthAmountResponse> data = List.of(monthAmount("Feb", 2000L));
                ApiResponse<List<TopupMonthAmountResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupAmountByCardService.findMonthlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsAmount.ApiResponseTopupMonthAmount response = handler
                                .findMonthlyTopupAmountsByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Feb");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(2000);
        }

        @Test
        @DisplayName("findMonthlyByCard - internal error")
        void findMonthlyByCard_Error() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .build();
                when(topupAmountByCardService.findMonthlyAmounts(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
                try {
                        handler.findMonthlyTopupAmountsByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findYearlyTopupAmountsByCardNumber ==========
        @Test
        @DisplayName("findYearlyByCard - success")
        void findYearlyByCard_Success() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .setCardNumber("1234").setYear(2024).build();

                List<TopupYearlyAmountResponse> data = List.of(yearlyAmount("2024", 6000L));
                ApiResponse<List<TopupYearlyAmountResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupAmountByCardService.findYearlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsAmount.ApiResponseTopupYearAmount response = handler
                                .findYearlyTopupAmountsByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(6000);
        }

        @Test
        @DisplayName("findYearlyByCard - internal error")
        void findYearlyByCard_Error() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .build();
                when(topupAmountByCardService.findYearlyAmounts(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
                try {
                        handler.findYearlyTopupAmountsByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== edge cases ==========
        @Test
        @DisplayName("findMonthly - null data still builds response")
        void findMonthly_NullData() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();
                ApiResponse<List<TopupMonthAmountResponse>> apiResp = ApiResponse.success("No data", null);
                when(topupAmountService.findMonthlyAmounts(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsAmount.ApiResponseTopupMonthAmount response = handler.findMonthlyTopupAmounts(request).await()
                                .indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isZero();
        }
}
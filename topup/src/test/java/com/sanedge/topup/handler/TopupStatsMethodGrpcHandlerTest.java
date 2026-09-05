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
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;
import com.sanedge.topup.service.stats.method.TopupMethodByCardService;
import com.sanedge.topup.service.stats.method.TopupMethodService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.topup.stats.TopupStatsMethod;

@ExtendWith(MockitoExtension.class)
class TopupStatsMethodGrpcHandlerTest {

        @Mock
        private TopupMethodService topupMethodService;

        @Mock
        private TopupMethodByCardService topupMethodByCardService;

        private TopupStatsMethodGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new TopupStatsMethodGrpcHandler();
                handler.topupMethodService = topupMethodService;
                handler.topupMethodByCardService = topupMethodByCardService;
        }

        // helpers
        private TopupMonthMethodResponse monthMethod(String month, String method, long count, long amount) {
                return new TopupMonthMethodResponse(month, method, count, amount);
        }

        private TopupYearlyMethodResponse yearlyMethod(String year, String method, long count, long amount) {
                return new TopupYearlyMethodResponse(year, method, count, amount);
        }

        @Test
        @DisplayName("findMonthlyTopupMethods - success")
        void findMonthly_Success() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();
                List<TopupMonthMethodResponse> data = List.of(monthMethod("Jan", "BANK_TRANSFER", 5, 10000L));
                ApiResponse<List<TopupMonthMethodResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupMethodService.findMonthlyMethods(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsMethod.ApiResponseTopupMonthMethod response = handler.findMonthlyTopupMethods(request).await()
                                .indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTopupMethod()).isEqualTo("BANK_TRANSFER");
                assertThat(response.getData(0).getTotalTopups()).isEqualTo(5);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(10000);
        }

        @Test
        @DisplayName("findMonthlyTopupMethods - error")
        void findMonthly_Error() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder().build();
                when(topupMethodService.findMonthlyMethods(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTopupMethods(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTopupMethods - success")
        void findYearly_Success() {
                pb.topup.Topup.FindYearTopupStatus request = pb.topup.Topup.FindYearTopupStatus.newBuilder()
                                .setYear(2024).build();
                List<TopupYearlyMethodResponse> data = List.of(yearlyMethod("2024", "BANK_TRANSFER", 10, 20000L));
                ApiResponse<List<TopupYearlyMethodResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupMethodService.findYearlyMethods(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsMethod.ApiResponseTopupYearMethod response = handler.findYearlyTopupMethods(request).await()
                                .indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalTopups()).isEqualTo(10);
        }

        @Test
        @DisplayName("findMonthlyByCardNumber - success")
        void findMonthlyByCard_Success() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .setCardNumber("1234").setYear(2024).build();
                List<TopupMonthMethodResponse> data = List.of(monthMethod("Feb", "QRIS", 2, 3000L));
                ApiResponse<List<TopupMonthMethodResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupMethodByCardService.findMonthlyMethods(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsMethod.ApiResponseTopupMonthMethod response = handler
                                .findMonthlyTopupMethodsByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTopupMethod()).isEqualTo("QRIS");
        }

        @Test
        @DisplayName("findYearlyByCardNumber - success")
        void findYearlyByCard_Success() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .setCardNumber("1234").setYear(2024).build();
                List<TopupYearlyMethodResponse> data = List.of(yearlyMethod("2024", "QRIS", 4, 6000L));
                ApiResponse<List<TopupYearlyMethodResponse>> apiResp = ApiResponse.success("Success", data);
                when(topupMethodByCardService.findYearlyMethods(any())).thenReturn(Uni.createFrom().item(apiResp));

                TopupStatsMethod.ApiResponseTopupYearMethod response = handler
                                .findYearlyTopupMethodsByCardNumber(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("findMonthlyByCard - error")
        void findMonthlyByCard_Error() {
                pb.topup.Topup.FindYearTopupCardNumber request = pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                                .build();
                when(topupMethodByCardService.findMonthlyMethods(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("Error")));
                try {
                        handler.findMonthlyTopupMethodsByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }
}
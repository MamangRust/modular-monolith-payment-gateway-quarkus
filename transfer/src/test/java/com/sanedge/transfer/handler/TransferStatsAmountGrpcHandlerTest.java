package com.sanedge.transfer.handler;

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
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.service.stats.amount.TransferAmountByCardService;
import com.sanedge.transfer.service.stats.amount.TransferAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.transfer.Transfer;
import pb.transfer.stats.TransferStatsAmount;

@ExtendWith(MockitoExtension.class)
class TransferStatsAmountGrpcHandlerTest {

        @Mock
        private TransferAmountService transferAmountService;

        @Mock
        private TransferAmountByCardService transferAmountByCardService;

        private TransferStatsAmountGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new TransferStatsAmountGrpcHandler();
                handler.transferAmountService = transferAmountService;
                handler.transferAmountByCardService = transferAmountByCardService;
        }

        // ========== Helper Methods ==========

        private TransferMonthAmountResponse createMonthAmountResponse(String month, Long amount) {
                TransferMonthAmountResponse response = new TransferMonthAmountResponse();
                response.setMonthName(month);
                response.setTotalAmount(amount);
                return response;
        }

        private TransferYearAmountResponse createYearAmountResponse(String year, Long amount) {
                TransferYearAmountResponse response = new TransferYearAmountResponse();
                response.setReportYear(year);
                response.setTotalAmount(amount);
                return response;
        }

        // ========== findMonthlyTransferAmounts ==========

        @Test
        @DisplayName("findMonthlyTransferAmounts - should return monthly amounts on success")
        void findMonthlyTransferAmounts_Success() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                List<TransferMonthAmountResponse> dataList = List.of(
                                createMonthAmountResponse("January", 1000000L),
                                createMonthAmountResponse("February", 1500000L),
                                createMonthAmountResponse("March", 2000000L));

                ApiResponse<List<TransferMonthAmountResponse>> apiResp = ApiResponse.success(
                                "Monthly amounts retrieved successfully", dataList);

                when(transferAmountService.findMonthlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferMonthAmount response = handler
                                .findMonthlyTransferAmounts(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(3);
                assertThat(response.getData(0).getMonth()).isEqualTo("January");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1000000);
        }

        @Test
        @DisplayName("findMonthlyTransferAmounts - should return INTERNAL on failure")
        void findMonthlyTransferAmounts_InternalError() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                when(transferAmountService.findMonthlyAmounts(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferAmounts(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferAmounts ==========

        @Test
        @DisplayName("findYearlyTransferAmounts - should return yearly amounts on success")
        void findYearlyTransferAmounts_Success() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                List<TransferYearAmountResponse> dataList = List.of(
                                createYearAmountResponse("2022", 10000000L),
                                createYearAmountResponse("2023", 15000000L),
                                createYearAmountResponse("2024", 20000000L));

                ApiResponse<List<TransferYearAmountResponse>> apiResp = ApiResponse.success(
                                "Yearly amounts retrieved successfully", dataList);

                when(transferAmountService.findYearlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferYearAmount response = handler.findYearlyTransferAmounts(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(3);
                assertThat(response.getData(2).getYear()).isEqualTo("2024");
                assertThat(response.getData(2).getTotalAmount()).isEqualTo(20000000);
        }

        @Test
        @DisplayName("findYearlyTransferAmounts - should return INTERNAL on failure")
        void findYearlyTransferAmounts_InternalError() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                when(transferAmountService.findYearlyAmounts(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferAmounts(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findMonthlyTransferAmountsBySenderCardNumber ==========

        @Test
        @DisplayName("findMonthlyTransferAmountsBySenderCardNumber - should return monthly amounts by sender on success")
        void findMonthlyTransferAmountsBySenderCardNumber_Success() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                List<TransferMonthAmountResponse> dataList = List.of(
                                createMonthAmountResponse("January", 500000L),
                                createMonthAmountResponse("February", 750000L));

                ApiResponse<List<TransferMonthAmountResponse>> apiResp = ApiResponse.success(
                                "Monthly sender amounts retrieved successfully", dataList);

                when(transferAmountByCardService.findMonthlyAmountsBySender(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferMonthAmount response = handler
                                .findMonthlyTransferAmountsBySenderCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("findMonthlyTransferAmountsBySenderCardNumber - should return INTERNAL on failure")
        void findMonthlyTransferAmountsBySenderCardNumber_InternalError() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                when(transferAmountByCardService.findMonthlyAmountsBySender(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferAmountsBySenderCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findMonthlyTransferAmountsByReceiverCardNumber ==========

        @Test
        @DisplayName("findMonthlyTransferAmountsByReceiverCardNumber - should return monthly amounts by receiver on success")
        void findMonthlyTransferAmountsByReceiverCardNumber_Success() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("6543210987654321")
                                .setYear(2024)
                                .build();

                List<TransferMonthAmountResponse> dataList = List.of(
                                createMonthAmountResponse("January", 300000L));

                ApiResponse<List<TransferMonthAmountResponse>> apiResp = ApiResponse.success(
                                "Monthly receiver amounts retrieved successfully", dataList);

                when(transferAmountByCardService.findMonthlyAmountsByReceiver(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferMonthAmount response = handler
                                .findMonthlyTransferAmountsByReceiverCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findMonthlyTransferAmountsByReceiverCardNumber - should return INTERNAL on failure")
        void findMonthlyTransferAmountsByReceiverCardNumber_InternalError() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("6543210987654321")
                                .setYear(2024)
                                .build();

                when(transferAmountByCardService.findMonthlyAmountsByReceiver(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferAmountsByReceiverCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferAmountsBySenderCardNumber ==========

        @Test
        @DisplayName("findYearlyTransferAmountsBySenderCardNumber - should return yearly amounts by sender on success")
        void findYearlyTransferAmountsBySenderCardNumber_Success() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                List<TransferYearAmountResponse> dataList = List.of(
                                createYearAmountResponse("2023", 5000000L),
                                createYearAmountResponse("2024", 8000000L));

                ApiResponse<List<TransferYearAmountResponse>> apiResp = ApiResponse.success(
                                "Yearly sender amounts retrieved successfully", dataList);

                when(transferAmountByCardService.findYearlyAmountsBySender(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferYearAmount response = handler
                                .findYearlyTransferAmountsBySenderCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(2);
                assertThat(response.getData(1).getTotalAmount()).isEqualTo(8000000);
        }

        @Test
        @DisplayName("findYearlyTransferAmountsBySenderCardNumber - should return INTERNAL on failure")
        void findYearlyTransferAmountsBySenderCardNumber_InternalError() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                when(transferAmountByCardService.findYearlyAmountsBySender(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferAmountsBySenderCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferAmountsByReceiverCardNumber ==========

        @Test
        @DisplayName("findYearlyTransferAmountsByReceiverCardNumber - should return yearly amounts by receiver on success")
        void findYearlyTransferAmountsByReceiverCardNumber_Success() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("6543210987654321")
                                .setYear(2024)
                                .build();

                List<TransferYearAmountResponse> dataList = List.of(
                                createYearAmountResponse("2024", 3000000L));

                ApiResponse<List<TransferYearAmountResponse>> apiResp = ApiResponse.success(
                                "Yearly receiver amounts retrieved successfully", dataList);

                when(transferAmountByCardService.findYearlyAmountsByReceiver(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferYearAmount response = handler
                                .findYearlyTransferAmountsByReceiverCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("findYearlyTransferAmountsByReceiverCardNumber - should return INTERNAL on failure")
        void findYearlyTransferAmountsByReceiverCardNumber_InternalError() {
                Transfer.FindByCardNumberTransferRequest request = Transfer.FindByCardNumberTransferRequest.newBuilder()
                                .setCardNumber("6543210987654321")
                                .setYear(2024)
                                .build();

                when(transferAmountByCardService.findYearlyAmountsByReceiver(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferAmountsByReceiverCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== Edge Cases ==========

        @Test
        @DisplayName("findMonthlyTransferAmounts - should return empty list when no data")
        void findMonthlyTransferAmounts_EmptyResult() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                ApiResponse<List<TransferMonthAmountResponse>> apiResp = ApiResponse.success(
                                "No data found", List.of());

                when(transferAmountService.findMonthlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferMonthAmount response = handler
                                .findMonthlyTransferAmounts(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("findMonthlyTransferAmounts - should handle null data gracefully")
        void findMonthlyTransferAmounts_NullData() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                ApiResponse<List<TransferMonthAmountResponse>> apiResp = ApiResponse.success("Success", null);

                when(transferAmountService.findMonthlyAmounts(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsAmount.ApiResponseTransferMonthAmount response = handler
                                .findMonthlyTransferAmounts(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(0);
        }
}
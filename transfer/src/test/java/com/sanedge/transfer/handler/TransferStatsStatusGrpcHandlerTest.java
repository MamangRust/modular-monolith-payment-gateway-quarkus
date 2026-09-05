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
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.transfer.service.stats.status.TransferStatusByCardService;
import com.sanedge.transfer.service.stats.status.TransferStatusService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.transfer.Transfer;
import pb.transfer.stats.TransferStatsStatus;

@ExtendWith(MockitoExtension.class)
class TransferStatsStatusGrpcHandlerTest {

        @Mock
        private TransferStatusService transferStatusService;

        @Mock
        private TransferStatusByCardService transferStatusByCardService;

        private TransferStatsStatusGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new TransferStatsStatusGrpcHandler();
                handler.transferStatusService = transferStatusService;
                handler.transferStatusByCardService = transferStatusByCardService;
        }

        // ========== Helper Methods ==========

        private TransferResponseMonthStatusSuccess createMonthStatusSuccess(String year, String month,
                        Long totalSuccess, Long totalAmount) {
                TransferResponseMonthStatusSuccess response = new TransferResponseMonthStatusSuccess();
                response.setReportYear(year);
                response.setMonthName(month);
                response.setTotalSuccess(totalSuccess);
                response.setTotalAmount(totalAmount);
                return response;
        }

        private TransferResponseMonthStatusFailed createMonthStatusFailed(String year, String month,
                        Long totalFailed, Long totalAmount) {
                TransferResponseMonthStatusFailed response = new TransferResponseMonthStatusFailed();
                response.setReportYear(year);
                response.setMonthName(month);
                response.setTotalFailed(totalFailed);
                response.setTotalAmount(totalAmount);
                return response;
        }

        private TransferResponseYearStatusSuccess createYearStatusSuccess(String year, Long totalSuccess,
                        Long totalAmount) {
                TransferResponseYearStatusSuccess response = new TransferResponseYearStatusSuccess();
                response.setReportYear(year);
                response.setTotalSuccess(totalSuccess);
                response.setTotalAmount(totalAmount);
                return response;
        }

        private TransferResponseYearStatusFailed createYearStatusFailed(String year, Long totalFailed,
                        Long totalAmount) {
                TransferResponseYearStatusFailed response = new TransferResponseYearStatusFailed();
                response.setReportYear(year);
                response.setTotalFailed(totalFailed);
                response.setTotalAmount(totalAmount);
                return response;
        }

        // ========== findMonthlyTransferStatusSuccess ==========

        @Test
        @DisplayName("findMonthlyTransferStatusSuccess - should return monthly success stats on success")
        void findMonthlyTransferStatusSuccess_Success() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                List<TransferResponseMonthStatusSuccess> dataList = List.of(
                                createMonthStatusSuccess("2024", "June", 150L, 7500000L));

                ApiResponse<List<TransferResponseMonthStatusSuccess>> apiResp = ApiResponse.success(
                                "Monthly success stats retrieved successfully", dataList);

                when(transferStatusService.findMonthStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusSuccess response = handler
                                .findMonthlyTransferStatusSuccess(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getMonth()).isEqualTo("June");
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(150);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(7500000);
        }

        @Test
        @DisplayName("findMonthlyTransferStatusSuccess - should return INTERNAL on failure")
        void findMonthlyTransferStatusSuccess_InternalError() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                when(transferStatusService.findMonthStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferStatusSuccess(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferStatusSuccess ==========

        @Test
        @DisplayName("findYearlyTransferStatusSuccess - should return yearly success stats on success")
        void findYearlyTransferStatusSuccess_Success() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                List<TransferResponseYearStatusSuccess> dataList = List.of(
                                createYearStatusSuccess("2022", 1000L, 50000000L),
                                createYearStatusSuccess("2023", 1200L, 60000000L),
                                createYearStatusSuccess("2024", 800L, 40000000L));

                ApiResponse<List<TransferResponseYearStatusSuccess>> apiResp = ApiResponse.success(
                                "Yearly success stats retrieved successfully", dataList);

                when(transferStatusService.findYearlyStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferYearStatusSuccess response = handler
                                .findYearlyTransferStatusSuccess(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(3);
                assertThat(response.getData(2).getTotalSuccess()).isEqualTo(800);
        }

        @Test
        @DisplayName("findYearlyTransferStatusSuccess - should return INTERNAL on failure")
        void findYearlyTransferStatusSuccess_InternalError() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                when(transferStatusService.findYearlyStatusSuccess(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferStatusSuccess(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findMonthlyTransferStatusFailed ==========

        @Test
        @DisplayName("findMonthlyTransferStatusFailed - should return monthly failed stats on success")
        void findMonthlyTransferStatusFailed_Success() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                List<TransferResponseMonthStatusFailed> dataList = List.of(
                                createMonthStatusFailed("2024", "June", 10L, 500000L));

                ApiResponse<List<TransferResponseMonthStatusFailed>> apiResp = ApiResponse.success(
                                "Monthly failed stats retrieved successfully", dataList);

                when(transferStatusService.findMonthStatusFailed(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusFailed response = handler
                                .findMonthlyTransferStatusFailed(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(10);
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(500000);
        }

        @Test
        @DisplayName("findMonthlyTransferStatusFailed - should return INTERNAL on failure")
        void findMonthlyTransferStatusFailed_InternalError() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                when(transferStatusService.findMonthStatusFailed(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferStatusFailed(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferStatusFailed ==========

        @Test
        @DisplayName("findYearlyTransferStatusFailed - should return yearly failed stats on success")
        void findYearlyTransferStatusFailed_Success() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                List<TransferResponseYearStatusFailed> dataList = List.of(
                                createYearStatusFailed("2022", 50L, 2500000L),
                                createYearStatusFailed("2023", 30L, 1500000L),
                                createYearStatusFailed("2024", 20L, 1000000L));

                ApiResponse<List<TransferResponseYearStatusFailed>> apiResp = ApiResponse.success(
                                "Yearly failed stats retrieved successfully", dataList);

                when(transferStatusService.findYearlyStatusFailed(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferYearStatusFailed response = handler
                                .findYearlyTransferStatusFailed(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(3);
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(50);
        }

        @Test
        @DisplayName("findYearlyTransferStatusFailed - should return INTERNAL on failure")
        void findYearlyTransferStatusFailed_InternalError() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                when(transferStatusService.findYearlyStatusFailed(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferStatusFailed(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findMonthlyTransferStatusSuccessByCardNumber ==========

        @Test
        @DisplayName("findMonthlyTransferStatusSuccessByCardNumber - should return monthly success stats by card on success")
        void findMonthlyTransferStatusSuccessByCardNumber_Success() {
                Transfer.FindMonthlyTransferStatusCardNumber request = Transfer.FindMonthlyTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                List<TransferResponseMonthStatusSuccess> dataList = List.of(
                                createMonthStatusSuccess("2024", "June", 50L, 2500000L));

                ApiResponse<List<TransferResponseMonthStatusSuccess>> apiResp = ApiResponse.success(
                                "Monthly success stats by card retrieved successfully", dataList);

                when(transferStatusByCardService.findMonthStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusSuccess response = handler
                                .findMonthlyTransferStatusSuccessByCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getTotalSuccess()).isEqualTo(50);
        }

        @Test
        @DisplayName("findMonthlyTransferStatusSuccessByCardNumber - should return INTERNAL on failure")
        void findMonthlyTransferStatusSuccessByCardNumber_InternalError() {
                Transfer.FindMonthlyTransferStatusCardNumber request = Transfer.FindMonthlyTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                when(transferStatusByCardService.findMonthStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferStatusSuccessByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferStatusSuccessByCardNumber ==========

        @Test
        @DisplayName("findYearlyTransferStatusSuccessByCardNumber - should return yearly success stats by card on success")
        void findYearlyTransferStatusSuccessByCardNumber_Success() {
                Transfer.FindYearTransferStatusCardNumber request = Transfer.FindYearTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                List<TransferResponseYearStatusSuccess> dataList = List.of(
                                createYearStatusSuccess("2023", 300L, 15000000L),
                                createYearStatusSuccess("2024", 200L, 10000000L));

                ApiResponse<List<TransferResponseYearStatusSuccess>> apiResp = ApiResponse.success(
                                "Yearly success stats by card retrieved successfully", dataList);

                when(transferStatusByCardService.findYearlyStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferYearStatusSuccess response = handler
                                .findYearlyTransferStatusSuccessByCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("findYearlyTransferStatusSuccessByCardNumber - should return INTERNAL on failure")
        void findYearlyTransferStatusSuccessByCardNumber_InternalError() {
                Transfer.FindYearTransferStatusCardNumber request = Transfer.FindYearTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                when(transferStatusByCardService.findYearlyStatusSuccessByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferStatusSuccessByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findMonthlyTransferStatusFailedByCardNumber ==========

        @Test
        @DisplayName("findMonthlyTransferStatusFailedByCardNumber - should return monthly failed stats by card on success")
        void findMonthlyTransferStatusFailedByCardNumber_Success() {
                Transfer.FindMonthlyTransferStatusCardNumber request = Transfer.FindMonthlyTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                List<TransferResponseMonthStatusFailed> dataList = List.of(
                                createMonthStatusFailed("2024", "June", 5L, 250000L));

                ApiResponse<List<TransferResponseMonthStatusFailed>> apiResp = ApiResponse.success(
                                "Monthly failed stats by card retrieved successfully", dataList);

                when(transferStatusByCardService.findMonthStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusFailed response = handler
                                .findMonthlyTransferStatusFailedByCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getTotalFailed()).isEqualTo(5);
        }

        @Test
        @DisplayName("findMonthlyTransferStatusFailedByCardNumber - should return INTERNAL on failure")
        void findMonthlyTransferStatusFailedByCardNumber_InternalError() {
                Transfer.FindMonthlyTransferStatusCardNumber request = Transfer.FindMonthlyTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                when(transferStatusByCardService.findMonthStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findMonthlyTransferStatusFailedByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== findYearlyTransferStatusFailedByCardNumber ==========

        @Test
        @DisplayName("findYearlyTransferStatusFailedByCardNumber - should return yearly failed stats by card on success")
        void findYearlyTransferStatusFailedByCardNumber_Success() {
                Transfer.FindYearTransferStatusCardNumber request = Transfer.FindYearTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                List<TransferResponseYearStatusFailed> dataList = List.of(
                                createYearStatusFailed("2023", 15L, 750000L),
                                createYearStatusFailed("2024", 10L, 500000L));

                ApiResponse<List<TransferResponseYearStatusFailed>> apiResp = ApiResponse.success(
                                "Yearly failed stats by card retrieved successfully", dataList);

                when(transferStatusByCardService.findYearlyStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferYearStatusFailed response = handler
                                .findYearlyTransferStatusFailedByCardNumber(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(2);
                assertThat(response.getData(1).getTotalFailed()).isEqualTo(10);
        }

        @Test
        @DisplayName("findYearlyTransferStatusFailedByCardNumber - should return INTERNAL on failure")
        void findYearlyTransferStatusFailedByCardNumber_InternalError() {
                Transfer.FindYearTransferStatusCardNumber request = Transfer.FindYearTransferStatusCardNumber
                                .newBuilder()
                                .setCardNumber("1234567890123456")
                                .setYear(2024)
                                .build();

                when(transferStatusByCardService.findYearlyStatusFailedByCard(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

                StatusRuntimeException ex = null;
                try {
                        handler.findYearlyTransferStatusFailedByCardNumber(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        ex = e;
                }

                assertThat(ex).isNotNull();
        }

        // ========== Edge Cases ==========

        @Test
        @DisplayName("findMonthlyTransferStatusSuccess - should return empty list when no data")
        void findMonthlyTransferStatusSuccess_EmptyResult() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(1)
                                .build();

                ApiResponse<List<TransferResponseMonthStatusSuccess>> apiResp = ApiResponse.success(
                                "No data found", List.of());

                when(transferStatusService.findMonthStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusSuccess response = handler
                                .findMonthlyTransferStatusSuccess(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("findMonthlyTransferStatusSuccess - should handle null data gracefully")
        void findMonthlyTransferStatusSuccess_NullData() {
                Transfer.FindMonthlyTransferStatus request = Transfer.FindMonthlyTransferStatus.newBuilder()
                                .setYear(2024)
                                .setMonth(6)
                                .build();

                ApiResponse<List<TransferResponseMonthStatusSuccess>> apiResp = ApiResponse.success("Success", null);

                when(transferStatusService.findMonthStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferMonthStatusSuccess response = handler
                                .findMonthlyTransferStatusSuccess(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("findYearlyTransferStatusSuccess - should return empty list when no data")
        void findYearlyTransferStatusSuccess_EmptyResult() {
                Transfer.FindYearTransferStatus request = Transfer.FindYearTransferStatus.newBuilder()
                                .setYear(2024)
                                .build();

                ApiResponse<List<TransferResponseYearStatusSuccess>> apiResp = ApiResponse.success(
                                "No data found", List.of());

                when(transferStatusService.findYearlyStatusSuccess(any())).thenReturn(Uni.createFrom().item(apiResp));

                TransferStatsStatus.ApiResponseTransferYearStatusSuccess response = handler
                                .findYearlyTransferStatusSuccess(request)
                                .await().indefinitely();

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(0);
        }
}
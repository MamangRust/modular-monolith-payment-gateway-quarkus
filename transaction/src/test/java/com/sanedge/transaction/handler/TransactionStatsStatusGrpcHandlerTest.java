package com.sanedge.transaction.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.requests.stats.MonthStatusTransaction;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;
import com.sanedge.transaction.service.stats.status.TransactionStatusByCardService;
import com.sanedge.transaction.service.stats.status.TransactionStatusService;

import io.smallrye.mutiny.Uni;
import pb.transaction.Transaction.FindMonthlyTransactionStatus;
import pb.transaction.Transaction.FindYearTransactionStatus;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusFailed;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusSuccess;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusFailed;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusSuccess;

/**
 * Unit tests for {@link TransactionStatsStatusGrpcHandler}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionStatsStatusGrpcHandlerTest {

    @Mock
    TransactionStatusService transactionStatusService;

    @Mock
    TransactionStatusByCardService transactionStatusByCardService;

    @InjectMocks
    TransactionStatsStatusGrpcHandler handler;

    private TransactionResponseMonthStatusSuccess monthStatusSuccess;
    private TransactionResponseYearStatusSuccess yearStatusSuccess;
    private TransactionResponseMonthStatusFailed monthStatusFailed;
    private TransactionResponseYearStatusFailed yearStatusFailed;

    @BeforeEach
    void setUp() {
        monthStatusSuccess = TransactionResponseMonthStatusSuccess.builder()
                .reportYear("2026")
                .monthName("Jun")
                .totalSuccess(10L)
                .totalAmount(2000000L)
                .build();

        yearStatusSuccess = TransactionResponseYearStatusSuccess.builder()
                .reportYear("2026")
                .totalSuccess(100L)
                .totalAmount(20000000L)
                .build();

        monthStatusFailed = TransactionResponseMonthStatusFailed.builder()
                .reportYear("2026")
                .monthName("Jun")
                .totalFailed(3L)
                .totalAmount(500000L)
                .build();

        yearStatusFailed = TransactionResponseYearStatusFailed.builder()
                .reportYear("2026")
                .totalFailed(5L)
                .totalAmount(1500000L)
                .build();
    }

    @Test
    void findMonthlyTransactionStatusSuccess_returnsData() {
        ApiResponse<List<TransactionResponseMonthStatusSuccess>> response = ApiResponse.success(
                "Monthly SUCCESS status retrieved successfully", List.of(monthStatusSuccess));
        when(transactionStatusService.findMonthStatusSuccess(any(MonthStatusTransaction.class)))
                .thenReturn(Uni.createFrom().item(response));

        FindMonthlyTransactionStatus request = FindMonthlyTransactionStatus.newBuilder()
                .setYear(2026)
                .setMonth(6)
                .build();
        ApiResponseTransactionMonthStatusSuccess result = handler.findMonthlyTransactionStatusSuccess(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getMonth()).isEqualTo("Jun");
        assertThat(result.getData(0).getTotalSuccess()).isEqualTo(10);
    }

    @Test
    void findYearlyTransactionStatusSuccess_returnsData() {
        ApiResponse<List<TransactionResponseYearStatusSuccess>> response = ApiResponse.success(
                "Yearly SUCCESS status retrieved successfully", List.of(yearStatusSuccess));
        when(transactionStatusService.findYearlyStatusSuccess(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionYearStatusSuccess result = handler.findYearlyTransactionStatusSuccess(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getYear()).isEqualTo("2026");
        assertThat(result.getData(0).getTotalSuccess()).isEqualTo(100);
    }

    @Test
    void findMonthlyTransactionStatusFailed_returnsData() {
        ApiResponse<List<TransactionResponseMonthStatusFailed>> response = ApiResponse.success(
                "Monthly FAILED status retrieved successfully", List.of(monthStatusFailed));
        when(transactionStatusService.findMonthStatusFailed(any(MonthStatusTransaction.class)))
                .thenReturn(Uni.createFrom().item(response));

        FindMonthlyTransactionStatus request = FindMonthlyTransactionStatus.newBuilder()
                .setYear(2026)
                .setMonth(6)
                .build();
        ApiResponseTransactionMonthStatusFailed result = handler.findMonthlyTransactionStatusFailed(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getMonth()).isEqualTo("Jun");
        assertThat(result.getData(0).getTotalFailed()).isEqualTo(3);
    }

    @Test
    void findYearlyTransactionStatusFailed_returnsData() {
        ApiResponse<List<TransactionResponseYearStatusFailed>> response = ApiResponse.success(
                "Yearly FAILED status retrieved successfully", List.of(yearStatusFailed));
        when(transactionStatusService.findYearlyStatusFailed(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionYearStatusFailed result = handler.findYearlyTransactionStatusFailed(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getYear()).isEqualTo("2026");
        assertThat(result.getData(0).getTotalFailed()).isEqualTo(5);
    }
}

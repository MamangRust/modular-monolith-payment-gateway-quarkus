package com.sanedge.transaction.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;
import com.sanedge.transaction.service.stats.amount.TransactionAmountByCardService;
import com.sanedge.transaction.service.stats.amount.TransactionAmountService;

import io.smallrye.mutiny.Uni;
import pb.transaction.Transaction.FindYearTransactionStatus;
import pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionMonthAmount;
import pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionYearAmount;

/**
 * Unit tests for {@link TransactionStatsAmountGrpcHandler}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionStatsAmountGrpcHandlerTest {

    @Mock
    TransactionAmountService transactionAmountService;

    @Mock
    TransactionAmountByCardService transactionAmountByCardService;

    @InjectMocks
    TransactionStatsAmountGrpcHandler handler;

    private TransactionMonthAmountResponse monthAmount;
    private TransactionYearlyAmountResponse yearAmount;

    @BeforeEach
    void setUp() {
        monthAmount = TransactionMonthAmountResponse.builder()
                .monthName("Jan")
                .totalAmount(1000000L)
                .build();

        yearAmount = TransactionYearlyAmountResponse.builder()
                .year("2026")
                .totalAmount(12000000L)
                .build();
    }

    @Test
    void findMonthlyAmounts_returnsData() {
        ApiResponse<List<TransactionMonthAmountResponse>> response = ApiResponse.success(
                "Monthly amounts retrieved successfully", List.of(monthAmount));
        when(transactionAmountService.findMonthlyAmounts(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionMonthAmount result = handler.findMonthlyAmounts(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getMonth()).isEqualTo("Jan");
        assertThat(result.getData(0).getTotalAmount()).isEqualTo(1000000);
    }

    @Test
    void findYearlyAmounts_returnsData() {
        ApiResponse<List<TransactionYearlyAmountResponse>> response = ApiResponse.success(
                "Yearly amounts retrieved successfully", List.of(yearAmount));
        when(transactionAmountService.findYearlyAmounts(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionYearAmount result = handler.findYearlyAmounts(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getYear()).isEqualTo("2026");
        assertThat(result.getData(0).getTotalAmount()).isEqualTo(12000000);
    }
}

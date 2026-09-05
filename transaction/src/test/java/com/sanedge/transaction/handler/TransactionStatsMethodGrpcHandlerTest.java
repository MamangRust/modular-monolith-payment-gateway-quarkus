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
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;
import com.sanedge.transaction.service.stats.method.TransactionMethodByCardService;
import com.sanedge.transaction.service.stats.method.TransactionMethodService;

import io.smallrye.mutiny.Uni;
import pb.transaction.Transaction.FindYearTransactionStatus;
import pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionMonthMethod;
import pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionYearMethod;

/**
 * Unit tests for {@link TransactionStatsMethodGrpcHandler}.
 */
@ExtendWith(MockitoExtension.class)
class TransactionStatsMethodGrpcHandlerTest {

    @Mock
    TransactionMethodService transactionMethodService;

    @Mock
    TransactionMethodByCardService transactionMethodByCardService;

    @InjectMocks
    TransactionStatsMethodGrpcHandler handler;

    private TransactionMonthMethodResponse monthMethod;
    private TransactionYearMethodResponse yearMethod;

    @BeforeEach
    void setUp() {
        monthMethod = TransactionMonthMethodResponse.builder()
                .monthName("Jan")
                .paymentMethod("BANK_TRANSFER")
                .totalTransactions(5L)
                .totalAmount(1000000L)
                .build();

        yearMethod = TransactionYearMethodResponse.builder()
                .reportYear("2026")
                .paymentMethod("BANK_TRANSFER")
                .totalTransactions(50L)
                .totalAmount(12000000L)
                .build();
    }

    @Test
    void findMonthlyPaymentMethods_returnsData() {
        ApiResponse<List<TransactionMonthMethodResponse>> response = ApiResponse.success(
                "Monthly payment methods retrieved successfully", List.of(monthMethod));
        when(transactionMethodService.findMonthlyMethod(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionMonthMethod result = handler.findMonthlyPaymentMethods(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getMonth()).isEqualTo("Jan");
        assertThat(result.getData(0).getPaymentMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(result.getData(0).getTotalAmount()).isEqualTo(1000000);
    }

    @Test
    void findYearlyPaymentMethods_returnsData() {
        ApiResponse<List<TransactionYearMethodResponse>> response = ApiResponse.success(
                "Yearly payment methods retrieved successfully", List.of(yearMethod));
        when(transactionMethodService.findYearlyMethod(2026L))
                .thenReturn(Uni.createFrom().item(response));

        FindYearTransactionStatus request = FindYearTransactionStatus.newBuilder().setYear(2026).build();
        ApiResponseTransactionYearMethod result = handler.findYearlyPaymentMethods(request)
                .await().indefinitely();

        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getDataCount()).isEqualTo(1);
        assertThat(result.getData(0).getYear()).isEqualTo("2026");
        assertThat(result.getData(0).getPaymentMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(result.getData(0).getTotalAmount()).isEqualTo(12000000);
    }
}

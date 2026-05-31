package com.sanedge.transaction.service.stats.amount;

import java.util.List;

import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;

import io.smallrye.mutiny.Uni;

public interface TransactionAmountByCardService {
    Uni<ApiResponse<List<TransactionMonthAmountResponse>>> findMonthlyAmounts(MonthYearPaymentMethod req);

    Uni<ApiResponse<List<TransactionYearlyAmountResponse>>> findYearlyAmounts(MonthYearPaymentMethod req);
}

package com.sanedge.transaction.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;

import io.smallrye.mutiny.Uni;

public interface TransactionAmountService {
    Uni<ApiResponse<List<TransactionMonthAmountResponse>>> findMonthlyAmounts(Long year);

    Uni<ApiResponse<List<TransactionYearlyAmountResponse>>> findYearlyAmounts(Long year);
}

package com.sanedge.transaction.service.stats.method;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;

import io.smallrye.mutiny.Uni;

public interface TransactionMethodService {
    Uni<ApiResponse<List<TransactionMonthMethodResponse>>> findMonthlyMethod(Long year);

    Uni<ApiResponse<List<TransactionYearMethodResponse>>> findYearlyMethod(Long year);
}

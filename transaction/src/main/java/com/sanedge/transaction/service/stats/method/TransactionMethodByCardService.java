package com.sanedge.transaction.service.stats.method;

import java.util.List;

import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;

import io.smallrye.mutiny.Uni;

public interface TransactionMethodByCardService {
    Uni<ApiResponse<List<TransactionMonthMethodResponse>>> findMonthlyMethod(MonthYearPaymentMethod req);

    Uni<ApiResponse<List<TransactionYearMethodResponse>>> findYearlyMethod(MonthYearPaymentMethod req);
}

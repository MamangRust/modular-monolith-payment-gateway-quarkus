package com.sanedge.transaction.service.stats.status;

import java.util.List;

import com.sanedge.transaction.domain.requests.stats.MonthStatusTransaction;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TransactionStatusService {
    Uni<ApiResponse<List<TransactionResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusTransaction req);

    Uni<ApiResponse<List<TransactionResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year);

    Uni<ApiResponse<List<TransactionResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusTransaction req);

    Uni<ApiResponse<List<TransactionResponseYearStatusFailed>>> findYearlyStatusFailed(Long year);
}

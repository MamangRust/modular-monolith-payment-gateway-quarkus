package com.sanedge.transaction.service.stats.status;

import java.util.List;

import com.sanedge.transaction.domain.requests.statsbycard.MonthStatusTransactionCardNumber;
import com.sanedge.transaction.domain.requests.statsbycard.YearStatusTransactionCardNumber;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TransactionStatusByCardService {
    Uni<ApiResponse<List<TransactionResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusTransactionCardNumber req);

    Uni<ApiResponse<List<TransactionResponseYearStatusSuccess>>> findYearlyStatusSuccess(YearStatusTransactionCardNumber req);

    Uni<ApiResponse<List<TransactionResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusTransactionCardNumber req);

    Uni<ApiResponse<List<TransactionResponseYearStatusFailed>>> findYearlyStatusFailed(YearStatusTransactionCardNumber req);
}

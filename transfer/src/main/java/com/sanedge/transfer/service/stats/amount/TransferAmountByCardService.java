package com.sanedge.transfer.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transfer.domain.requests.statsbycard.MonthYearCardNumber;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;

import io.smallrye.mutiny.Uni;

public interface TransferAmountByCardService {
    Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsBySender(MonthYearCardNumber req);

    Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmountsByReceiver(MonthYearCardNumber req);

    Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsBySender(MonthYearCardNumber req);

    Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmountsByReceiver(MonthYearCardNumber req);
}

package com.sanedge.transfer.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;

import io.smallrye.mutiny.Uni;

public interface TransferAmountService {
    Uni<ApiResponse<List<TransferMonthAmountResponse>>> findMonthlyAmounts(Long year);

    Uni<ApiResponse<List<TransferYearAmountResponse>>> findYearlyAmounts(Long year);
}

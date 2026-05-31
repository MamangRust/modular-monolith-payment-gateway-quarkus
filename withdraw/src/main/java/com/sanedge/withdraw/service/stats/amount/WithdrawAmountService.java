package com.sanedge.withdraw.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;

import io.smallrye.mutiny.Uni;

public interface WithdrawAmountService {
    Uni<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyWithdraws(Long year);

    Uni<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyWithdraws(Long year);
}

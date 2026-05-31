package com.sanedge.withdraw.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.requests.statsbycard.YearMonthCardNumber;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;

import io.smallrye.mutiny.Uni;

public interface WithdrawAmountByCardService {
    Uni<ApiResponse<List<WithdrawMonthlyAmountResponse>>> findMonthlyByCardNumber(YearMonthCardNumber req);

    Uni<ApiResponse<List<WithdrawYearlyAmountResponse>>> findYearlyByCardNumber(YearMonthCardNumber req);
}

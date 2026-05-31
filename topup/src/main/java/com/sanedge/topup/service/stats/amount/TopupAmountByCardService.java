package com.sanedge.topup.service.stats.amount;

import java.util.List;

import com.sanedge.topup.domain.requests.stats.YearMonthMethod;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupMonthAmountResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupYearlyAmountResponse;

import io.smallrye.mutiny.Uni;

public interface TopupAmountByCardService {
    Uni<ApiResponse<List<TopupMonthAmountResponse>>> findMonthlyAmounts(YearMonthMethod req);

    Uni<ApiResponse<List<TopupYearlyAmountResponse>>> findYearlyAmounts(YearMonthMethod req);
}

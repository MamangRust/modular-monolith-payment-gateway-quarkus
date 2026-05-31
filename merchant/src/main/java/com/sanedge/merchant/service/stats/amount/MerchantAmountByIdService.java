package com.sanedge.merchant.service.stats.amount;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyid.MonthYearAmountMerchant;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantAmountByIdService {
    Uni<ApiResponse<List<MerchantResponseMonthlyAmount>>> findMonthAmountById(MonthYearAmountMerchant req);

    Uni<ApiResponse<List<MerchantResponseYearlyAmount>>> findYearAmountById(MonthYearAmountMerchant req);
}

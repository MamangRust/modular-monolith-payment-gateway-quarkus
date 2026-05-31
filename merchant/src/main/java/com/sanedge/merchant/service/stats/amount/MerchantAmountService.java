package com.sanedge.merchant.service.stats.amount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantAmountService {
    Uni<ApiResponse<List<MerchantResponseMonthlyAmount>>> findMonthAmount(Long year);

    Uni<ApiResponse<List<MerchantResponseYearlyAmount>>> findYearAmount(Long year);
}

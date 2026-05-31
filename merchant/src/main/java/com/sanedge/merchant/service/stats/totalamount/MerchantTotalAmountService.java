package com.sanedge.merchant.service.stats.totalamount;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantTotalAmountService {
    Uni<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> findMonthTotalAmount(Long year);

    Uni<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> findYearTotalAmount(Long year);
}

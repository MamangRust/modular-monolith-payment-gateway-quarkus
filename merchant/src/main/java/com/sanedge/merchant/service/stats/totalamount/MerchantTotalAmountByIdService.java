package com.sanedge.merchant.service.stats.totalamount;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyid.MonthYearTotalAmountMerchant;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantTotalAmountByIdService {
    Uni<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> findMonthTotalAmountById(MonthYearTotalAmountMerchant req);

    Uni<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> findYearTotalAmountById(MonthYearTotalAmountMerchant req);
}

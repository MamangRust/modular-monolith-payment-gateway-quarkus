package com.sanedge.merchant.service.stats.totalamount;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearTotalAmountApiKey;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantTotalAmountByApiKeyService {
    Uni<ApiResponse<List<MerchantResponseMonthlyTotalAmount>>> findMonthTotalAmountByApiKey(MonthYearTotalAmountApiKey req);

    Uni<ApiResponse<List<MerchantResponseYearlyTotalAmount>>> findYearTotalAmountByApiKey(MonthYearTotalAmountApiKey req);
}

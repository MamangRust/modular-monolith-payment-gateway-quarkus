package com.sanedge.merchant.service.stats.amount;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearAmountApiKey;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;

import io.smallrye.mutiny.Uni;

public interface MerchantAmountByApiKeyService {
    Uni<ApiResponse<List<MerchantResponseMonthlyAmount>>> findMonthAmountByApiKey(MonthYearAmountApiKey req);

    Uni<ApiResponse<List<MerchantResponseYearlyAmount>>> findYearAmountByApiKey(MonthYearAmountApiKey req);
}

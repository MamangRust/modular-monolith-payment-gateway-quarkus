package com.sanedge.merchant.service.stats.method;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearPaymentMethodApiKey;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;

import io.smallrye.mutiny.Uni;

public interface MerchantMethodByApiKeyService {
    Uni<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> findMonthMethod(MonthYearPaymentMethodApiKey req);

    Uni<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> findYearMethod(MonthYearPaymentMethodApiKey req);
}

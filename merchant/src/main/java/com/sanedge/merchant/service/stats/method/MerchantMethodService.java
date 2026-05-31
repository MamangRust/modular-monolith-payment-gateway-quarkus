package com.sanedge.merchant.service.stats.method;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;

import io.smallrye.mutiny.Uni;

public interface MerchantMethodService {
    Uni<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> findMonthMethod(Long year);

    Uni<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> findYearMethod(Long year);
}

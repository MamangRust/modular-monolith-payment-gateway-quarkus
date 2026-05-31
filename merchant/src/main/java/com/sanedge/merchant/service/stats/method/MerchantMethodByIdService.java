package com.sanedge.merchant.service.stats.method;

import java.util.List;

import com.sanedge.merchant.domain.requests.statsbyid.MonthYearPaymentMethodMerchant;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;

import io.smallrye.mutiny.Uni;

public interface MerchantMethodByIdService {
    Uni<ApiResponse<List<MerchantResponseMonthlyPaymentMethod>>> findMonthMethodById(MonthYearPaymentMethodMerchant req);

    Uni<ApiResponse<List<MerchantResponseYearlyPaymentMethod>>> findYearMethodById(MonthYearPaymentMethodMerchant req);
}

package com.sanedge.topup.service.stats.method;

import java.util.List;

import com.sanedge.topup.domain.requests.stats.YearMonthMethod;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;

import io.smallrye.mutiny.Uni;

public interface TopupMethodByCardService {
    Uni<ApiResponse<List<TopupMonthMethodResponse>>> findMonthlyMethods(YearMonthMethod req);

    Uni<ApiResponse<List<TopupYearlyMethodResponse>>> findYearlyMethods(YearMonthMethod req);
}

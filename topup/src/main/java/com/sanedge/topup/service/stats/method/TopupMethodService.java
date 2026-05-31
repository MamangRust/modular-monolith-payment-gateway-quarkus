package com.sanedge.topup.service.stats.method;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;

import io.smallrye.mutiny.Uni;

public interface TopupMethodService {
    Uni<ApiResponse<List<TopupMonthMethodResponse>>> findMonthlyMethods(Long year);

    Uni<ApiResponse<List<TopupYearlyMethodResponse>>> findYearlyMethods(Long year);
}

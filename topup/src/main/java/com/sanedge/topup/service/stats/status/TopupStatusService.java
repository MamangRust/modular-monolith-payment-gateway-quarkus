package com.sanedge.topup.service.stats.status;

import java.util.List;

import com.sanedge.topup.domain.requests.stats.MonthTopupStatus;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TopupStatusService {
    Uni<ApiResponse<List<TopupResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthTopupStatus req);

    Uni<ApiResponse<List<TopupResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year);

    Uni<ApiResponse<List<TopupResponseMonthStatusFailed>>> findMonthStatusFailed(MonthTopupStatus req);

    Uni<ApiResponse<List<TopupResponseYearStatusFailed>>> findYearlyStatusFailed(Long year);
}

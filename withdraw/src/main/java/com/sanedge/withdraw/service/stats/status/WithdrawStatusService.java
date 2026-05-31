package com.sanedge.withdraw.service.stats.status;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.requests.MonthStatusWithdraw;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface WithdrawStatusService {
    Uni<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusWithdraw req);

    Uni<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year);

    Uni<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusWithdraw req);

    Uni<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailed(Long year);
}

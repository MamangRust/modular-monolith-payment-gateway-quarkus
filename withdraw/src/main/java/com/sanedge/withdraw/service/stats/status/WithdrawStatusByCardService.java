package com.sanedge.withdraw.service.stats.status;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.withdraw.domain.requests.statsbycard.MonthStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.requests.statsbycard.YearStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface WithdrawStatusByCardService {
    Uni<ApiResponse<List<WithdrawResponseMonthStatusSuccess>>> findMonthStatusSuccessByCard(
            MonthStatusWithdrawCardNumber req);

    Uni<ApiResponse<List<WithdrawResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(
            YearStatusWithdrawCardNumber req);

    Uni<ApiResponse<List<WithdrawResponseMonthStatusFailed>>> findMonthStatusFailedByCard(
            MonthStatusWithdrawCardNumber req);

    Uni<ApiResponse<List<WithdrawResponseYearStatusFailed>>> findYearlyStatusFailedByCard(
            YearStatusWithdrawCardNumber req);
}

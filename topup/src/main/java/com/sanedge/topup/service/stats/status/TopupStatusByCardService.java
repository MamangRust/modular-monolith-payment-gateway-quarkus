package com.sanedge.topup.service.stats.status;

import java.util.List;

import com.sanedge.topup.domain.requests.statsbycard.MonthTopupStatusCardNumber;
import com.sanedge.topup.domain.requests.statsbycard.YearTopupStatusCardNumber;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TopupStatusByCardService {
    Uni<ApiResponse<List<TopupResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthTopupStatusCardNumber req);

    Uni<ApiResponse<List<TopupResponseYearStatusSuccess>>> findYearlyStatusSuccess(YearTopupStatusCardNumber req);

    Uni<ApiResponse<List<TopupResponseMonthStatusFailed>>> findMonthStatusFailed(MonthTopupStatusCardNumber req);

    Uni<ApiResponse<List<TopupResponseYearStatusFailed>>> findYearlyStatusFailed(YearTopupStatusCardNumber req);
}

package com.sanedge.transfer.service.stats.status;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transfer.domain.requests.stats.MonthStatusTransfer;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TransferStatusService {
    Uni<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthStatusSuccess(MonthStatusTransfer req);

    Uni<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthStatusFailed(MonthStatusTransfer req);

    Uni<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccess(Long year);

    Uni<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailed(Long year);
}

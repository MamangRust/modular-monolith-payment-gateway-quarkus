package com.sanedge.transfer.service.stats.status;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.transfer.domain.requests.statsbycard.YearStatusTransferCardNumber;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;

import io.smallrye.mutiny.Uni;

public interface TransferStatusByCardService {
    Uni<ApiResponse<List<TransferResponseMonthStatusSuccess>>> findMonthStatusSuccessByCard(MonthStatusTransferCardNumber req);

    Uni<ApiResponse<List<TransferResponseYearStatusSuccess>>> findYearlyStatusSuccessByCard(YearStatusTransferCardNumber req);

    Uni<ApiResponse<List<TransferResponseMonthStatusFailed>>> findMonthStatusFailedByCard(MonthStatusTransferCardNumber req);

    Uni<ApiResponse<List<TransferResponseYearStatusFailed>>> findYearlyStatusFailedByCard(YearStatusTransferCardNumber req);
}

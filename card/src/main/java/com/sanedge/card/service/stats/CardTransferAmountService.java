package com.sanedge.card.service.stats;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import io.smallrye.mutiny.Uni;

public interface CardTransferAmountService {
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmountSender(Long year);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmountSender(Long year);
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmountReceiver(Long year);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmountReceiver(Long year);
}

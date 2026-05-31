package com.sanedge.card.service.statsbycard;

import java.util.List;

import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import io.smallrye.mutiny.Uni;

public interface CardTransferAmountByCardService {
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmountSender(MonthYearCardNumberCard req);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmountSender(MonthYearCardNumberCard req);
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmountReceiver(MonthYearCardNumberCard req);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmountReceiver(MonthYearCardNumberCard req);
}

package com.sanedge.card.service.statsbycard;

import java.util.List;

import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import io.smallrye.mutiny.Uni;

public interface CardTransactionAmountByCardService {
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmountByCard(MonthYearCardNumberCard req);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmountByCard(MonthYearCardNumberCard req);
}

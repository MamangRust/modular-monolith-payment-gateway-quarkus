package com.sanedge.card.service.statsbycard;

import java.util.List;

import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;

import io.smallrye.mutiny.Uni;

public interface CardBalanceByCardService {
    Uni<ApiResponse<List<CardResponseMonthBalance>>> findMonthBalanceByCard(MonthYearCardNumberCard req);
    Uni<ApiResponse<List<CardResponseYearBalance>>> findYearBalanceByCard(MonthYearCardNumberCard req);
}

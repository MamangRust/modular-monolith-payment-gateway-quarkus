package com.sanedge.card.service.stats;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;

import io.smallrye.mutiny.Uni;

public interface CardBalanceService {
    Uni<ApiResponse<List<CardResponseMonthBalance>>> findMonthBalance(Long year);
    Uni<ApiResponse<List<CardResponseYearBalance>>> findYearBalance(Long year);
}

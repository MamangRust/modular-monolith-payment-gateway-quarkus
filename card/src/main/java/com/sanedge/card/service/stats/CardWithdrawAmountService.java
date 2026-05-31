package com.sanedge.card.service.stats;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import io.smallrye.mutiny.Uni;

public interface CardWithdrawAmountService {
    Uni<ApiResponse<List<CardResponseMonthAmount>>> findMonthAmount(Long year);
    Uni<ApiResponse<List<CardResponseYearAmount>>> findYearAmount(Long year);
}

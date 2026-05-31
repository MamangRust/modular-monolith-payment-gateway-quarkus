package com.sanedge.card.service;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.dashboard.CardDashboard;
import com.sanedge.card.domain.response.dashboard.CardDashboardCard;

import io.smallrye.mutiny.Uni;

public interface CardDashboardService {
    Uni<ApiResponse<CardDashboard>> dashboard();
    Uni<ApiResponse<CardDashboardCard>> dashboardByCard(String cardNumber);
}

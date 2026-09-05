package com.sanedge.card.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.response.dashboard.CardDashboard;
import com.sanedge.card.domain.response.dashboard.CardDashboardCard;
import com.sanedge.card.repository.CardDashboardByCardRepository;
import com.sanedge.card.repository.CardDashboardRepository;
import com.sanedge.card.service.CardDashboardService;
import com.sanedge.common.observability.TracingMetrics;

import io.opentelemetry.api.common.Attributes;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CardDashboardServiceImpl implements CardDashboardService {
    private static final Logger logger = LoggerFactory.getLogger(CardDashboardServiceImpl.class);

    private final CardDashboardRepository cardDashboardRepository;
    private final CardDashboardByCardRepository cardDashboardByCardRepository;
    private final TracingMetrics tracingMetrics;

    @Inject
    public CardDashboardServiceImpl(CardDashboardRepository cardDashboardRepository,
                                   CardDashboardByCardRepository cardDashboardByCardRepository,
                                   TracingMetrics tracingMetrics) {
        this.cardDashboardRepository = cardDashboardRepository;
        this.cardDashboardByCardRepository = cardDashboardByCardRepository;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    public Uni<ApiResponse<CardDashboard>> dashboard() {
        Attributes attrs = Attributes.builder()
                .put("operation", "get_global_dashboard")
                .build();

        return tracingMetrics.traceAndMeasure("getGlobalDashboard", "get_global_dashboard", attrs, () -> {
            logger.info("📊 Fetching global dashboard statistics");

            return Uni.combine().all().unis(
                cardDashboardRepository.getTotalBalance(),
                cardDashboardRepository.getTotalTopupAmount(),
                cardDashboardRepository.getTotalTransactionAmount(),
                cardDashboardRepository.getTotalTransferAmount(),
                cardDashboardRepository.getTotalWithdrawAmount()
            ).asTuple().map(tuple -> {
                CardDashboard dashboard = CardDashboard.builder()
                        .totalBalance(tuple.getItem1())
                        .totalTopup(tuple.getItem2())
                        .totalTransaction(tuple.getItem3())
                        .totalTransfer(tuple.getItem4())
                        .totalWithdraw(tuple.getItem5())
                        .build();

                logger.info("✅ Global dashboard retrieved successfully");
                return ApiResponse.success("Global dashboard retrieved successfully", dashboard);
            });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch dashboard data", e);
        });
    }

    @Override
    public Uni<ApiResponse<CardDashboardCard>> dashboardByCard(String cardNumber) {
        Attributes attrs = Attributes.builder()
                .put("operation", "get_dashboard_by_card")
                .put("card.number", cardNumber)
                .build();

        return tracingMetrics.traceAndMeasure("getDashboardByCard", "get_dashboard_by_card", attrs, () -> {
            logger.info("💳📊 Fetching dashboard for card: {}", cardNumber);

            return Uni.combine().all().unis(
                cardDashboardByCardRepository.getTotalBalanceByCard(cardNumber),
                cardDashboardByCardRepository.getTotalTopupAmountByCard(cardNumber),
                cardDashboardByCardRepository.getTotalTransactionAmountByCard(cardNumber),
                cardDashboardByCardRepository.getTotalTransferAmountBySender(cardNumber),
                cardDashboardByCardRepository.getTotalTransferAmountByReceiver(cardNumber),
                cardDashboardByCardRepository.getTotalWithdrawAmountByCard(cardNumber)
            ).asTuple().map(tuple -> {
                CardDashboardCard dashboardCard = CardDashboardCard.builder()
                        .totalBalance(tuple.getItem1())
                        .totalTopup(tuple.getItem2())
                        .totalTransaction(tuple.getItem3())
                        .totalTransferSend(tuple.getItem4())
                        .totalTransferReceiver(tuple.getItem5())
                        .totalWithdraw(tuple.getItem6())
                        .build();

                logger.info("✅ Dashboard for card {} retrieved successfully", cardNumber);
                return ApiResponse.success("Dashboard for card " + cardNumber + " retrieved successfully", dashboardCard);
            });
        }).onFailure().invoke(e -> {
            logger.error("❌ Failed to fetch dashboard for card {}", cardNumber, e);
        });
    }
}
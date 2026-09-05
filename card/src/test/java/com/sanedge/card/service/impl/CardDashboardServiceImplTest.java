package com.sanedge.card.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.card.repository.CardDashboardByCardRepository;
import com.sanedge.card.repository.CardDashboardRepository;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.observability.TracingMetrics;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardDashboardServiceImplTest {

    @Mock
    private CardDashboardRepository dashboardRepo;
    @Mock
    private CardDashboardByCardRepository byCardRepo;

    private TracingMetrics tracingMetrics;
    private CardDashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        tracingMetrics = mock(TracingMetrics.class, withSettings().lenient());
        service = new CardDashboardServiceImpl(dashboardRepo, byCardRepo, tracingMetrics);

        // Lenient stubs for traceAndMeasure - handle both 3-param and 4-param overloads
        lenient().doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Supplier<Uni<?>> s = inv.getArgument(inv.getArguments().length - 1);
            return s.get();
        }).when(tracingMetrics).traceAndMeasure(anyString(), anyString(), any());

        lenient().doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            Supplier<Uni<?>> s = inv.getArgument(inv.getArguments().length - 1);
            return s.get();
        }).when(tracingMetrics).traceAndMeasure(anyString(), anyString(), any(io.opentelemetry.api.common.Attributes.class), any());
    }

    @Test
    @DisplayName("dashboard - returns aggregated values")
    void dashboardSuccess() {
        when(dashboardRepo.getTotalBalance()).thenReturn(Uni.createFrom().item(1000L));
        when(dashboardRepo.getTotalTopupAmount()).thenReturn(Uni.createFrom().item(2000L));
        when(dashboardRepo.getTotalTransactionAmount()).thenReturn(Uni.createFrom().item(3000L));
        when(dashboardRepo.getTotalTransferAmount()).thenReturn(Uni.createFrom().item(4000L));
        when(dashboardRepo.getTotalWithdrawAmount()).thenReturn(Uni.createFrom().item(5000L));

        ApiResponse<com.sanedge.card.domain.response.dashboard.CardDashboard> resp = service.dashboard().await()
                .indefinitely();
        assertThat(resp.status()).isEqualTo("success");
        assertThat(resp.data().getTotalBalance()).isEqualTo(1000L);
        assertThat(resp.data().getTotalTopup()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("dashboardByCard - returns aggregated values per card")
    void dashboardByCardSuccess() {
        when(byCardRepo.getTotalBalanceByCard("CARD")).thenReturn(Uni.createFrom().item(100L));
        when(byCardRepo.getTotalTopupAmountByCard("CARD")).thenReturn(Uni.createFrom().item(200L));
        when(byCardRepo.getTotalTransactionAmountByCard("CARD")).thenReturn(Uni.createFrom().item(300L));
        when(byCardRepo.getTotalTransferAmountBySender("CARD")).thenReturn(Uni.createFrom().item(400L));
        when(byCardRepo.getTotalTransferAmountByReceiver("CARD")).thenReturn(Uni.createFrom().item(500L));
        when(byCardRepo.getTotalWithdrawAmountByCard("CARD")).thenReturn(Uni.createFrom().item(600L));

        ApiResponse<com.sanedge.card.domain.response.dashboard.CardDashboardCard> resp = service.dashboardByCard("CARD")
                .await().indefinitely();
        assertThat(resp.status()).isEqualTo("success");
        assertThat(resp.data().getTotalBalance()).isEqualTo(100L);
        assertThat(resp.data().getTotalTransferSend()).isEqualTo(400L);
    }
}
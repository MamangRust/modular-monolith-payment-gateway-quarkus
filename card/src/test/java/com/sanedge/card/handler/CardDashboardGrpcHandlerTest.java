package com.sanedge.card.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.protobuf.Empty;
import com.sanedge.card.domain.response.dashboard.CardDashboard;
import com.sanedge.card.domain.response.dashboard.CardDashboardCard;
import com.sanedge.card.service.CardDashboardService;
import com.sanedge.common.domain.response.ApiResponse;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.card.Card;

@ExtendWith(MockitoExtension.class)
class CardDashboardGrpcHandlerTest {

    @Mock
    private CardDashboardService dashboardService;

    private CardDashboardGrpcHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CardDashboardGrpcHandler();
        handler.cardDashboardService = dashboardService;
    }

    @Test
    @DisplayName("dashboardCard - success")
    void dashboardCard_Success() {
        CardDashboard dash = CardDashboard.builder()
                .totalBalance(1000L).totalTopup(2000L).totalWithdraw(500L)
                .totalTransaction(3000L).totalTransfer(1500L).build();
        ApiResponse<CardDashboard> apiResp = ApiResponse.success("Dashboard", dash);
        when(dashboardService.dashboard()).thenReturn(Uni.createFrom().item(apiResp));

        pb.card.CardDashboard.ApiResponseDashboardCard response = handler.dashboardCard(Empty.getDefaultInstance())
                .await()
                .indefinitely();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getData().getTotalBalance()).isEqualTo(1000);
        assertThat(response.getData().getTotalTopup()).isEqualTo(2000);
    }

    @Test
    @DisplayName("dashboardCard - error")
    void dashboardCard_Error() {
        when(dashboardService.dashboard()).thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
        try {
            handler.dashboardCard(Empty.getDefaultInstance()).await().indefinitely();
        } catch (StatusRuntimeException e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    @DisplayName("dashboardCardNumber - success")
    void dashboardCardNumber_Success() {
        CardDashboardCard cardDash = CardDashboardCard.builder()
                .totalBalance(100L).totalTopup(200L).totalWithdraw(50L)
                .totalTransaction(300L).totalTransferSend(400L).totalTransferReceiver(500L).build();
        ApiResponse<CardDashboardCard> apiResp = ApiResponse.success("Dashboard by card", cardDash);
        when(dashboardService.dashboardByCard(anyString())).thenReturn(Uni.createFrom().item(apiResp));

        pb.card.CardDashboard.ApiResponseDashboardCardNumber response = handler.dashboardCardNumber(
                Card.FindByCardNumberRequest.newBuilder().setCardNumber("CARD-1").build()).await().indefinitely();
        assertThat(response.getStatus()).isEqualTo("success");
        assertThat(response.getData().getTotalBalance()).isEqualTo(100);
        assertThat(response.getData().getTotalTransferSend()).isEqualTo(400);
    }

    @Test
    @DisplayName("dashboardCardNumber - error")
    void dashboardCardNumber_Error() {
        when(dashboardService.dashboardByCard(anyString()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("fail")));
        try {
            handler.dashboardCardNumber(Card.FindByCardNumberRequest.newBuilder().setCardNumber("CARD-1").build())
                    .await().indefinitely();
        } catch (StatusRuntimeException e) {
            assertThat(e).isNotNull();
        }
    }
}
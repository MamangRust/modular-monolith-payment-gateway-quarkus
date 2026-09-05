package com.sanedge.gateway.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.CardDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private pb.card.MutinyCardQueryServiceGrpc.MutinyCardQueryServiceStub cardQueryService;

    @Mock
    private pb.card.MutinyCardCommandServiceGrpc.MutinyCardCommandServiceStub cardCommandService;

    @Mock
    private pb.card.MutinyCardDashboardServiceGrpc.MutinyCardDashboardServiceStub cardDashboardService;

    @Mock
    private pb.card.stats.MutinyCardStatsBalanceServiceGrpc.MutinyCardStatsBalanceServiceStub cardStatsBalanceService;

    @Mock
    private pb.card.stats.MutinyCardStatsTopupServiceGrpc.MutinyCardStatsTopupServiceStub cardStatsTopupService;

    @Mock
    private pb.card.stats.MutinyCardStatsTransactionServiceGrpc.MutinyCardStatsTransactionServiceStub cardStatsTransactionService;

    @Mock
    private pb.card.stats.MutinyCardStatsTransferServiceGrpc.MutinyCardStatsTransferServiceStub cardStatsTransferService;

    @Mock
    private pb.card.stats.MutinyCardStatsWithdrawServiceGrpc.MutinyCardStatsWithdrawServiceStub cardStatsWithdrawService;

    private CardServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Uni<?>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        service = new CardServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("cardQueryService", cardQueryService);
        inject("cardCommandService", cardCommandService);
        inject("cardDashboardService", cardDashboardService);
        inject("cardStatsBalanceService", cardStatsBalanceService);
        inject("cardStatsTopupService", cardStatsTopupService);
        inject("cardStatsTransactionService", cardStatsTransactionService);
        inject("cardStatsTransferService", cardStatsTransferService);
        inject("cardStatsWithdrawService", cardStatsWithdrawService);
    }

    private void inject(String name, Object value) throws Exception {
        Field f = CardServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void listCards_PropagatesPaginationResponse() {
        pb.card.CardQuery.ApiResponsePaginationCard proto = pb.card.CardQuery.ApiResponsePaginationCard.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(cardQueryService.findAllCard(any(pb.card.Card.FindAllCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponsePaginationCard result = service.listCards(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findActiveCards_PropagatesResponse() {
        pb.card.CardQuery.ApiResponsePaginationCardDeleteAt proto = pb.card.CardQuery.ApiResponsePaginationCardDeleteAt.newBuilder()
                .setStatus("success").build();
        lenient().when(cardQueryService.findByActiveCard(any(pb.card.Card.FindAllCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponsePaginationCardDeleteAt result = service.findActiveCards(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findTrashedCards_PropagatesResponse() {
        pb.card.CardQuery.ApiResponsePaginationCardDeleteAt proto = pb.card.CardQuery.ApiResponsePaginationCardDeleteAt.newBuilder()
                .setStatus("success").build();
        lenient().when(cardQueryService.findByTrashedCard(any(pb.card.Card.FindAllCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponsePaginationCardDeleteAt result = service.findTrashedCards(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void getCard_PropagatesResponse() {
        pb.card.Card.ApiResponseCard proto = pb.card.Card.ApiResponseCard.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(cardQueryService.findByIdCard(any(pb.card.Card.FindByIdCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponseCard result = service.getCard(1).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void createCard_PropagatesResponse() {
        CardDto.CreateRequest req = new CardDto.CreateRequest(1, "VISA", "2025-12-31", "123", "BCA");
        pb.card.Card.ApiResponseCard proto = pb.card.Card.ApiResponseCard.newBuilder()
                .setStatus("success").setMessage("created").build();
        lenient().when(cardCommandService.createCard(any(pb.card.CardCommand.CreateCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponseCard result = service.createCard(req).await().indefinitely();
        assertThat(result.message()).isEqualTo("created");
    }

    @Test
    void deleteCard_PropagatesResponse() {
        pb.card.Card.ApiResponseCardDeleteAt proto = pb.card.Card.ApiResponseCardDeleteAt.newBuilder()
                .setStatus("success").setMessage("trashed").build();
        lenient().when(cardCommandService.trashedCard(any(pb.card.Card.FindByIdCardRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponseCardDeleteAt result = service.deleteCard(1).await().indefinitely();
        assertThat(result.message()).isEqualTo("trashed");
    }

    @Test
    void findMonthlyBalance_PropagatesResponse() {
        pb.card.stats.CardStatsBalance.ApiResponseMonthlyBalance proto = pb.card.stats.CardStatsBalance.ApiResponseMonthlyBalance.newBuilder()
                .setStatus("success").build();
        lenient().when(cardStatsBalanceService.findMonthlyBalance(any(pb.card.stats.CardStatsBalance.FindYearBalance.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponseMonthlyBalance result = service.findMonthlyBalance(2024).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findCardDashboard_PropagatesResponse() {
        pb.card.CardDashboard.ApiResponseDashboardCard proto = pb.card.CardDashboard.ApiResponseDashboardCard.newBuilder()
                .setStatus("success").build();
        lenient().when(cardDashboardService.dashboardCard(any(com.google.protobuf.Empty.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        CardDto.ApiResponseDashboardCard result = service.findCardDashboard().await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }
}

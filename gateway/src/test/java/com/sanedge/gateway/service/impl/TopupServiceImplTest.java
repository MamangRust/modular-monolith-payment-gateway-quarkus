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

import com.sanedge.gateway.dto.TopupDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TopupServiceImplTest {

    @Mock private TelemetryHelper telemetryHelper;
    @Mock private pb.topup.MutinyTopupQueryServiceGrpc.MutinyTopupQueryServiceStub topupQueryService;
    @Mock private pb.topup.MutinyTopupCommandServiceGrpc.MutinyTopupCommandServiceStub topupCommandService;
    @Mock private pb.topup.stats.MutinyTopupStatsAmountServiceGrpc.MutinyTopupStatsAmountServiceStub topupStatsAmountService;
    @Mock private pb.topup.stats.MutinyTopupStatsMethodServiceGrpc.MutinyTopupStatsMethodServiceStub topupStatsMethodService;
    @Mock private pb.topup.stats.MutinyTopupStatsStatusServiceGrpc.MutinyTopupStatsStatusServiceStub topupStatsStatusService;

    private TopupServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> { Supplier<Uni<?>> s = inv.getArgument(1); return s.get(); });
        service = new TopupServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("topupQueryService", topupQueryService);
        inject("topupCommandService", topupCommandService);
        inject("topupStatsAmountService", topupStatsAmountService);
        inject("topupStatsMethodService", topupStatsMethodService);
        inject("topupStatsStatusService", topupStatsStatusService);
    }

    private void inject(String n, Object v) throws Exception {
        Field f = TopupServiceImpl.class.getDeclaredField(n);
        f.setAccessible(true); f.set(service, v);
    }

    @Test void listTopups_PropagatesResponse() {
        pb.topup.TopupQuery.ApiResponsePaginationTopup proto = pb.topup.TopupQuery.ApiResponsePaginationTopup.newBuilder().setStatus("success").build();
        lenient().when(topupQueryService.findAllTopup(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.listTopups(1, 10, "").await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getTopup_PropagatesResponse() {
        pb.topup.Topup.ApiResponseTopup proto = pb.topup.Topup.ApiResponseTopup.newBuilder().setStatus("success").build();
        lenient().when(topupQueryService.findByIdTopup(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getTopup(1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void createTopup_PropagatesResponse() {
        TopupDto.CreateRequest req = new TopupDto.CreateRequest("1234567890", 50000, "BANK_TRANSFER");
        pb.topup.Topup.ApiResponseTopup proto = pb.topup.Topup.ApiResponseTopup.newBuilder().setStatus("success").setMessage("created").build();
        lenient().when(topupCommandService.createTopup(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.createTopup(req).await().indefinitely().message()).isEqualTo("created");
    }

    @Test void getMonthlyAmounts_PropagatesResponse() {
        pb.topup.stats.TopupStatsAmount.ApiResponseTopupMonthAmount proto = pb.topup.stats.TopupStatsAmount.ApiResponseTopupMonthAmount.newBuilder().setStatus("success").build();
        lenient().when(topupStatsAmountService.findMonthlyTopupAmounts(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyAmounts(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMonthlyMethods_PropagatesResponse() {
        pb.topup.stats.TopupStatsMethod.ApiResponseTopupMonthMethod proto = pb.topup.stats.TopupStatsMethod.ApiResponseTopupMonthMethod.newBuilder().setStatus("success").build();
        lenient().when(topupStatsMethodService.findMonthlyTopupMethods(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyMethods(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMonthlyStatusSuccess_PropagatesResponse() {
        pb.topup.stats.TopupStatsStatus.ApiResponseTopupMonthStatusSuccess proto = pb.topup.stats.TopupStatsStatus.ApiResponseTopupMonthStatusSuccess.newBuilder().setStatus("success").build();
        lenient().when(topupStatsStatusService.findMonthlyTopupStatusSuccess(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyStatusSuccess(2024, 6).await().indefinitely().status()).isEqualTo("success");
    }
}

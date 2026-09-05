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

import com.sanedge.gateway.dto.SaldoDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class SaldoServiceImplTest {

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private pb.saldo.MutinySaldoQueryServiceGrpc.MutinySaldoQueryServiceStub saldoQueryService;

    @Mock
    private pb.saldo.MutinySaldoCommandServiceGrpc.MutinySaldoCommandServiceStub saldoCommandService;

    @Mock
    private pb.saldo.stats.MutinySaldoStatsBalanceServiceGrpc.MutinySaldoStatsBalanceServiceStub saldoStatsBalanceService;

    @Mock
    private pb.saldo.stats.MutinySaldoStatsTotalBalanceGrpc.MutinySaldoStatsTotalBalanceStub saldoStatsTotalBalanceService;

    private SaldoServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Uni<?>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        service = new SaldoServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("saldoQueryService", saldoQueryService);
        inject("saldoCommandService", saldoCommandService);
        inject("saldoStatsBalanceService", saldoStatsBalanceService);
        inject("saldoStatsTotalBalanceService", saldoStatsTotalBalanceService);
    }

    private void inject(String name, Object value) throws Exception {
        Field f = SaldoServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void listSaldos_PropagatesPaginationResponse() {
        pb.saldo.SaldoQuery.ApiResponsePaginationSaldo proto = pb.saldo.SaldoQuery.ApiResponsePaginationSaldo.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(saldoQueryService.findAllSaldo(any(pb.saldo.Saldo.FindAllSaldoRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponsePaginationSaldo result = service.listSaldos(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void getSaldo_PropagatesResponse() {
        pb.saldo.Saldo.ApiResponseSaldo proto = pb.saldo.Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success").setMessage("ok").build();
        lenient().when(saldoQueryService.findByIdSaldo(any(pb.saldo.Saldo.FindByIdSaldoRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseSaldo result = service.getSaldo(1).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void createSaldo_PropagatesResponse() {
        SaldoDto.CreateRequest req = new SaldoDto.CreateRequest("1234567890", 100000);
        pb.saldo.Saldo.ApiResponseSaldo proto = pb.saldo.Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success").setMessage("created").build();
        lenient().when(saldoCommandService.createSaldo(any(pb.saldo.SaldoCommand.CreateSaldoRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseSaldo result = service.createSaldo(req).await().indefinitely();
        assertThat(result.message()).isEqualTo("created");
    }

    @Test
    void updateSaldo_PropagatesResponse() {
        SaldoDto.UpdateRequest req = new SaldoDto.UpdateRequest(1, "1234567890", 200000);
        pb.saldo.Saldo.ApiResponseSaldo proto = pb.saldo.Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success").setMessage("updated").build();
        lenient().when(saldoCommandService.updateSaldo(any(pb.saldo.SaldoCommand.UpdateSaldoRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseSaldo result = service.updateSaldo(1, req).await().indefinitely();
        assertThat(result.message()).isEqualTo("updated");
    }

    @Test
    void deleteSaldo_PropagatesResponse() {
        pb.saldo.Saldo.ApiResponseSaldoDeleteAt proto = pb.saldo.Saldo.ApiResponseSaldoDeleteAt.newBuilder()
                .setStatus("success").setMessage("trashed").build();
        lenient().when(saldoCommandService.trashedSaldo(any(pb.saldo.Saldo.FindByIdSaldoRequest.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseSaldoDeleteAt result = service.deleteSaldo(1).await().indefinitely();
        assertThat(result.message()).isEqualTo("trashed");
    }

    @Test
    void findMonthlySaldoBalances_PropagatesResponse() {
        pb.saldo.stats.SaldoStatsBalance.ApiResponseMonthSaldoBalances proto = pb.saldo.stats.SaldoStatsBalance.ApiResponseMonthSaldoBalances.newBuilder()
                .setStatus("success").build();
        lenient().when(saldoStatsBalanceService.findMonthlySaldoBalances(any(pb.saldo.Saldo.FindYearlySaldo.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseMonthSaldoBalances result = service.findMonthlySaldoBalances(2024).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findYearlySaldoBalances_PropagatesResponse() {
        pb.saldo.stats.SaldoStatsBalance.ApiResponseYearSaldoBalances proto = pb.saldo.stats.SaldoStatsBalance.ApiResponseYearSaldoBalances.newBuilder()
                .setStatus("success").build();
        lenient().when(saldoStatsBalanceService.findYearlySaldoBalances(any(pb.saldo.Saldo.FindYearlySaldo.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseYearSaldoBalances result = service.findYearlySaldoBalances(2024).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findMonthlyTotalSaldoBalance_PropagatesResponse() {
        pb.saldo.stats.SaldoStatsTotal.ApiResponseMonthTotalSaldo proto = pb.saldo.stats.SaldoStatsTotal.ApiResponseMonthTotalSaldo.newBuilder()
                .setStatus("success").build();
        lenient().when(saldoStatsTotalBalanceService.findMonthlyTotalSaldoBalance(any(pb.saldo.Saldo.FindMonthlySaldoTotalBalance.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseMonthTotalSaldo result = service.findMonthlyTotalSaldoBalance(2024, 6).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void findYearTotalSaldoBalance_PropagatesResponse() {
        pb.saldo.stats.SaldoStatsTotal.ApiResponseYearTotalSaldo proto = pb.saldo.stats.SaldoStatsTotal.ApiResponseYearTotalSaldo.newBuilder()
                .setStatus("success").build();
        lenient().when(saldoStatsTotalBalanceService.findYearTotalSaldoBalance(any(pb.saldo.Saldo.FindYearlySaldo.class)))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        SaldoDto.ApiResponseYearTotalSaldo result = service.findYearTotalSaldoBalance(2024).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }
}

package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.SaldoDto;
import com.sanedge.gateway.service.SaldoService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SaldoServiceImpl implements SaldoService {

    private static final Logger LOG = Logger.getLogger(SaldoServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("saldo")
    pb.saldo.MutinySaldoQueryServiceGrpc.MutinySaldoQueryServiceStub saldoQueryService;

    @GrpcClient("saldo")
    pb.saldo.MutinySaldoCommandServiceGrpc.MutinySaldoCommandServiceStub saldoCommandService;

    @GrpcClient("saldo")
    pb.saldo.stats.MutinySaldoStatsBalanceServiceGrpc.MutinySaldoStatsBalanceServiceStub saldoStatsBalanceService;

    @GrpcClient("saldo")
    pb.saldo.stats.MutinySaldoStatsTotalBalanceGrpc.MutinySaldoStatsTotalBalanceStub saldoStatsTotalBalanceService;

    @Override
    public Uni<SaldoDto.ApiResponsePaginationSaldo> listSaldos(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("saldo.listSaldos", () -> saldoQueryService.findAllSaldo(pb.saldo.Saldo.FindAllSaldoRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(SaldoDto.ApiResponsePaginationSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list saldos: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseSaldo> getSaldo(int id) {
        return telemetryHelper.traceAndMetric("saldo.getSaldo", () -> saldoQueryService.findByIdSaldo(pb.saldo.Saldo.FindByIdSaldoRequest.newBuilder()
                .setSaldoId(id)
                .build())
                .map(SaldoDto.ApiResponseSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get saldo with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseSaldo> createSaldo(SaldoDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("saldo.createSaldo", () -> saldoCommandService.createSaldo(pb.saldo.SaldoCommand.CreateSaldoRequest.newBuilder()
                .setCardNumber(body.cardNumber())
                .setTotalBalance(body.totalBalance())
                .build())
                .map(SaldoDto.ApiResponseSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create saldo: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseSaldo> updateSaldo(int id, SaldoDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("saldo.updateSaldo", () -> saldoCommandService.updateSaldo(pb.saldo.SaldoCommand.UpdateSaldoRequest.newBuilder()
                .setSaldoId(id)
                .setCardNumber(body.cardNumber())
                .setTotalBalance(body.totalBalance())
                .build())
                .map(SaldoDto.ApiResponseSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update saldo: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseSaldoDeleteAt> deleteSaldo(int id) {
        return telemetryHelper.traceAndMetric("saldo.deleteSaldo", () -> saldoCommandService.trashedSaldo(pb.saldo.Saldo.FindByIdSaldoRequest.newBuilder()
                .setSaldoId(id)
                .build())
                .map(SaldoDto.ApiResponseSaldoDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete saldo: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseMonthSaldoBalances> findMonthlySaldoBalances(int year) {
        return telemetryHelper.traceAndMetric("saldo.findMonthlySaldoBalances", () -> saldoStatsBalanceService.findMonthlySaldoBalances(pb.saldo.Saldo.FindYearlySaldo.newBuilder()
                .setYear(year)
                .build())
                .map(SaldoDto.ApiResponseMonthSaldoBalances::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly saldo balance stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseYearSaldoBalances> findYearlySaldoBalances(int year) {
        return telemetryHelper.traceAndMetric("saldo.findYearlySaldoBalances", () -> saldoStatsBalanceService.findYearlySaldoBalances(pb.saldo.Saldo.FindYearlySaldo.newBuilder()
                .setYear(year)
                .build())
                .map(SaldoDto.ApiResponseYearSaldoBalances::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly saldo balance stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseMonthTotalSaldo> findMonthlyTotalSaldoBalance(int year, int month) {
        return telemetryHelper.traceAndMetric("saldo.findMonthlyTotalSaldoBalance", () -> saldoStatsTotalBalanceService.findMonthlyTotalSaldoBalance(pb.saldo.Saldo.FindMonthlySaldoTotalBalance.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build())
                .map(SaldoDto.ApiResponseMonthTotalSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly total saldo balance: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<SaldoDto.ApiResponseYearTotalSaldo> findYearTotalSaldoBalance(int year) {
        return telemetryHelper.traceAndMetric("saldo.findYearTotalSaldoBalance", () -> saldoStatsTotalBalanceService.findYearTotalSaldoBalance(pb.saldo.Saldo.FindYearlySaldo.newBuilder()
                .setYear(year)
                .build())
                .map(SaldoDto.ApiResponseYearTotalSaldo::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly total saldo balance: " + throwable.getMessage(), throwable)));
    }
}

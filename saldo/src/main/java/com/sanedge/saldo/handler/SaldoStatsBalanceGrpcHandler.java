package com.sanedge.saldo.handler;

import com.sanedge.saldo.domain.response.stats.balance.SaldoMonthBalanceResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoYearBalanceResponse;
import com.sanedge.saldo.service.stats.SaldoBalanceService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.saldo.stats.MutinySaldoStatsBalanceServiceGrpc;
import pb.saldo.stats.SaldoStatsBalance.ApiResponseMonthSaldoBalances;
import pb.saldo.stats.SaldoStatsBalance.ApiResponseYearSaldoBalances;

@GrpcService
@Singleton
public class SaldoStatsBalanceGrpcHandler extends MutinySaldoStatsBalanceServiceGrpc.SaldoStatsBalanceServiceImplBase {

    @Inject
    SaldoBalanceService saldoBalanceService;

    @Override
    public Uni<ApiResponseMonthSaldoBalances> findMonthlySaldoBalances(pb.saldo.Saldo.FindYearlySaldo request) {
        return saldoBalanceService.getMonthBalance((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMonthSaldoBalances.Builder builder = ApiResponseMonthSaldoBalances.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (SaldoMonthBalanceResponse r : apiResp.data()) {
                            builder.addData(pb.saldo.stats.SaldoStatsBalance.SaldoMonthBalanceResponse.newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setTotalBalance(r.getTotalBalance().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearSaldoBalances> findYearlySaldoBalances(pb.saldo.Saldo.FindYearlySaldo request) {
        return saldoBalanceService.getYearBalance((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseYearSaldoBalances.Builder builder = ApiResponseYearSaldoBalances.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (SaldoYearBalanceResponse r : apiResp.data()) {
                            builder.addData(pb.saldo.stats.SaldoStatsBalance.SaldoYearBalanceResponse.newBuilder()
                                    .setYear(r.getReportYear())
                                    .setTotalBalance(r.getTotalBalance().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

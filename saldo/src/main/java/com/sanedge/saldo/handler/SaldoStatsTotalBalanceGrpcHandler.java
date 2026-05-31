package com.sanedge.saldo.handler;

import com.sanedge.saldo.domain.requests.MonthTotalSaldoBalance;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoMonthTotalBalanceResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoYearTotalBalanceResponse;
import com.sanedge.saldo.service.stats.SaldoTotalAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.saldo.stats.MutinySaldoStatsTotalBalanceGrpc;
import pb.saldo.stats.SaldoStatsTotal.ApiResponseMonthTotalSaldo;
import pb.saldo.stats.SaldoStatsTotal.ApiResponseYearTotalSaldo;

@GrpcService
@Singleton
public class SaldoStatsTotalBalanceGrpcHandler extends MutinySaldoStatsTotalBalanceGrpc.SaldoStatsTotalBalanceImplBase {

    @Inject
    SaldoTotalAmountService saldoTotalAmountService;

    @Override
    public Uni<ApiResponseMonthTotalSaldo> findMonthlyTotalSaldoBalance(
            pb.saldo.Saldo.FindMonthlySaldoTotalBalance request) {
        MonthTotalSaldoBalance req = new MonthTotalSaldoBalance();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return saldoTotalAmountService.findMonthTotalBalance(req)
                .map(apiResp -> {
                    ApiResponseMonthTotalSaldo.Builder builder = ApiResponseMonthTotalSaldo.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (SaldoMonthTotalBalanceResponse r : apiResp.data()) {
                            builder.addData(pb.saldo.stats.SaldoStatsTotal.SaldoMonthTotalBalanceResponse.newBuilder()
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
    public Uni<ApiResponseYearTotalSaldo> findYearTotalSaldoBalance(pb.saldo.Saldo.FindYearlySaldo request) {
        return saldoTotalAmountService.findYearTotalBalance((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseYearTotalSaldo.Builder builder = ApiResponseYearTotalSaldo.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (SaldoYearTotalBalanceResponse r : apiResp.data()) {
                            builder.addData(pb.saldo.stats.SaldoStatsTotal.SaldoYearTotalBalanceResponse.newBuilder()
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

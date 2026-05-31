package com.sanedge.saldo.service.stats;

import java.util.List;

import com.sanedge.saldo.domain.requests.MonthTotalSaldoBalance;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoMonthTotalBalanceResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoYearTotalBalanceResponse;

import io.smallrye.mutiny.Uni;

public interface SaldoTotalAmountService {
    Uni<ApiResponse<List<SaldoMonthTotalBalanceResponse>>> findMonthTotalBalance(MonthTotalSaldoBalance req);

    Uni<ApiResponse<List<SaldoYearTotalBalanceResponse>>> findYearTotalBalance(Long year);
}

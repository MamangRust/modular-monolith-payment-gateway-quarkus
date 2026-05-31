package com.sanedge.saldo.service.stats;

import java.util.List;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoMonthBalanceResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoYearBalanceResponse;

import io.smallrye.mutiny.Uni;

public interface SaldoBalanceService {
    Uni<ApiResponse<List<SaldoMonthBalanceResponse>>> getMonthBalance(Long year);

    Uni<ApiResponse<List<SaldoYearBalanceResponse>>> getYearBalance(Long year);
}

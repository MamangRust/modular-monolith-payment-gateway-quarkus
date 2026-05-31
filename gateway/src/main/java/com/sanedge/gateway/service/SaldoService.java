package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.SaldoDto;
import io.smallrye.mutiny.Uni;

public interface SaldoService {
    Uni<SaldoDto.ApiResponsePaginationSaldo> listSaldos(int page, int size, String search);
    Uni<SaldoDto.ApiResponseSaldo> getSaldo(int id);
    Uni<SaldoDto.ApiResponseSaldo> createSaldo(SaldoDto.CreateRequest body);
    Uni<SaldoDto.ApiResponseSaldo> updateSaldo(int id, SaldoDto.UpdateRequest body);
    Uni<SaldoDto.ApiResponseSaldoDeleteAt> deleteSaldo(int id);
    Uni<SaldoDto.ApiResponseMonthSaldoBalances> findMonthlySaldoBalances(int year);
    Uni<SaldoDto.ApiResponseYearSaldoBalances> findYearlySaldoBalances(int year);
    Uni<SaldoDto.ApiResponseMonthTotalSaldo> findMonthlyTotalSaldoBalance(int year, int month);
    Uni<SaldoDto.ApiResponseYearTotalSaldo> findYearTotalSaldoBalance(int year);
}

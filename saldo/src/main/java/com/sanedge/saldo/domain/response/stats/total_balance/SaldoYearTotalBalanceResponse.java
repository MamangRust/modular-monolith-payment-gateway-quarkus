package com.sanedge.saldo.domain.response.stats.total_balance;

import com.sanedge.saldo.entity.SaldoYearTotalBalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoYearTotalBalanceResponse {
    private String reportYear;
    private Long totalBalance;

    public static SaldoYearTotalBalanceResponse from(SaldoYearTotalBalance dto) {
        return SaldoYearTotalBalanceResponse.builder()
                .reportYear(dto.getYear())
                .totalBalance(dto.getTotalBalance().longValue())
                .build();
    }
}

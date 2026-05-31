package com.sanedge.saldo.domain.response.stats.balance;

import com.sanedge.saldo.entity.SaldoYearBalance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoYearBalanceResponse {
    private String reportYear;
    private Long totalBalance;

    public static SaldoYearBalanceResponse from(SaldoYearBalance dto) {
        return SaldoYearBalanceResponse.builder()
                .reportYear(dto.getYear())
                .totalBalance(dto.getTotalBalance().longValue())
                .build();
    }
}
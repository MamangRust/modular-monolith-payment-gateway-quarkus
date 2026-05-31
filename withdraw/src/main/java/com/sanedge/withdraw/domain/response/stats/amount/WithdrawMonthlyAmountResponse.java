package com.sanedge.withdraw.domain.response.stats.amount;

import com.sanedge.withdraw.entity.WithdrawMonthlyAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawMonthlyAmountResponse {
    private String monthName;
    private Long totalAmount;

    public static WithdrawMonthlyAmountResponse from(WithdrawMonthlyAmount dto) {
        return WithdrawMonthlyAmountResponse.builder()
                .monthName(dto.getMonth())
                .totalAmount(dto.getTotalAmount())
                .build();
    }
}
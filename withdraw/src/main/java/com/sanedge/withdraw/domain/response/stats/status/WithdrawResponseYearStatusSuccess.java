package com.sanedge.withdraw.domain.response.stats.status;

import com.sanedge.withdraw.entity.WithdrawYearStatusSuccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponseYearStatusSuccess {
    private String reportYear;
    private Long totalAmount;
    private Long totalSuccess;

    public static WithdrawResponseYearStatusSuccess from(WithdrawYearStatusSuccess dto) {
        return WithdrawResponseYearStatusSuccess.builder()
                .reportYear(dto.getYear())
                .totalAmount(dto.getTotalAmount())
                .totalSuccess(dto.getTotalSuccess().longValue())
                .build();
    }
}

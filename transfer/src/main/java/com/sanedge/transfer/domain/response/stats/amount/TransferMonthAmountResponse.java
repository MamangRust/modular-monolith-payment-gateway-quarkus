package com.sanedge.transfer.domain.response.stats.amount;

import com.sanedge.transfer.entity.TransferMonthAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferMonthAmountResponse {
    private String monthName;
    private Long totalAmount;

    public static TransferMonthAmountResponse from(TransferMonthAmount dto) {
        return TransferMonthAmountResponse.builder()
                .monthName(dto.getMonth())
                .totalAmount(dto.getTotalAmount())
                .build();
    }
}
package com.sanedge.topup.domain.response.stats.amount;

import com.sanedge.topup.entity.TopupMonthAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupMonthAmountResponse {
    private String monthName;
    private Long totalAmount;

    public static TopupMonthAmountResponse from(TopupMonthAmount dto) {
        return TopupMonthAmountResponse.builder()
                .monthName(dto.getMonth())
                .totalAmount(dto.getTotalAmount().longValue())
                .build();
    }
}
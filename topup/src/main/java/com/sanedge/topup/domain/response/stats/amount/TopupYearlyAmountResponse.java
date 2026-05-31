package com.sanedge.topup.domain.response.stats.amount;

import com.sanedge.topup.entity.TopupYearAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopupYearlyAmountResponse {
    private String reportYear;
    private Long totalAmount;

    public static TopupYearlyAmountResponse from(TopupYearAmount dto) {
        return TopupYearlyAmountResponse.builder()
                .reportYear(dto.getYear())
                .totalAmount(dto.getTotalAmount().longValue())
                .build();
    }
}
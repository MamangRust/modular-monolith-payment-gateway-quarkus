package com.sanedge.transaction.domain.response.stats.method;

import com.sanedge.transaction.entity.TransactionYearMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionYearMethodResponse {
    private String reportYear;
    private String paymentMethod;
    private Long totalTransactions;
    private Long totalAmount;

    public static TransactionYearMethodResponse from(TransactionYearMethod dto) {
        return TransactionYearMethodResponse.builder()
                .reportYear(dto.getYear())
                .paymentMethod(dto.getPaymentMethod())
                .totalTransactions(dto.getTotalTransactions().longValue())
                .totalAmount(dto.getTotalAmount())
                .build();
    }
}

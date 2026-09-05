package com.sanedge.transaction.domain.response.stats.amount;

import com.sanedge.transaction.entity.TransactionYearAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionYearlyAmountResponse {
    private String year;
    private Long totalAmount;

    public TransactionYearlyAmountResponse(String year, long totalAmount) {
        this.year = year;
        this.totalAmount = totalAmount;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static TransactionYearlyAmountResponse from(TransactionYearAmount dto) {
        return new TransactionYearlyAmountResponse(
            dto.getYear(),
            dto.getTotalAmount()
        );
    }
}
package com.sanedge.transaction.domain.response.stats.amount;

import com.sanedge.transaction.entity.TransactionMonthAmount;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonthAmountResponse {
    private String monthName;
    private Long totalAmount;

    public TransactionMonthAmountResponse(String monthName, long totalAmount) {
        this.monthName = monthName;
        this.totalAmount = totalAmount;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static TransactionMonthAmountResponse from(TransactionMonthAmount dto) {
        return new TransactionMonthAmountResponse(
            dto.getMonth(),
            dto.getTotalAmount()
        );
    }
}
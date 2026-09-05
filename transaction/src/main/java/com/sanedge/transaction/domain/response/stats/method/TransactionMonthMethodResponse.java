package com.sanedge.transaction.domain.response.stats.method;

import com.sanedge.transaction.entity.TransactionMonthMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonthMethodResponse {
    private String monthName;
    private String paymentMethod;
    private Long totalTransactions;
    private Long totalAmount;

    public TransactionMonthMethodResponse(String monthName, String paymentMethod, long totalTransactions, long totalAmount) {
        this.monthName = monthName;
        this.paymentMethod = paymentMethod;
        this.totalTransactions = totalTransactions;
        this.totalAmount = totalAmount;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static TransactionMonthMethodResponse from(TransactionMonthMethod dto) {
        return new TransactionMonthMethodResponse(
            dto.getMonth(),
            dto.getPaymentMethod(),
            dto.getTotalTransactions(),
            dto.getTotalAmount()
        );
    }
}
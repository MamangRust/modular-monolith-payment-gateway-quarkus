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

    public TransactionYearMethodResponse(String reportYear, String paymentMethod, long totalTransactions, long totalAmount) {
        this.reportYear = reportYear;
        this.paymentMethod = paymentMethod;
        this.totalTransactions = totalTransactions;
        this.totalAmount = totalAmount;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
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

    public static TransactionYearMethodResponse from(TransactionYearMethod dto) {
        return new TransactionYearMethodResponse(
            dto.getYear(),
            dto.getPaymentMethod(),
            dto.getTotalTransactions(),
            dto.getTotalAmount()
        );
    }
}
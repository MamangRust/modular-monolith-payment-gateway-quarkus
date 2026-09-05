package com.sanedge.transaction.domain.response.stats.status;

import com.sanedge.transaction.entity.TransactionYearStatusSuccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseYearStatusSuccess {
    private String reportYear;
    private Long totalAmount;
    private Long totalSuccess;

    public TransactionResponseYearStatusSuccess(String reportYear, long totalAmount, long totalSuccess) {
        this.reportYear = reportYear;
        this.totalAmount = totalAmount;
        this.totalSuccess = totalSuccess;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(Long totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public static TransactionResponseYearStatusSuccess from(TransactionYearStatusSuccess dto) {
        return new TransactionResponseYearStatusSuccess(
            dto.getYear(),
            dto.getTotalAmount(),
            dto.getTotalSuccess()
        );
    }
}
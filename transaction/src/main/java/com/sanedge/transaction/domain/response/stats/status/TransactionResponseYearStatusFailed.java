package com.sanedge.transaction.domain.response.stats.status;

import com.sanedge.transaction.entity.TransactionYearStatusFailed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseYearStatusFailed {
    private String reportYear;
    private Long totalAmount;
    private Long totalFailed;

    public TransactionResponseYearStatusFailed(String reportYear, long totalAmount, long totalFailed) {
        this.reportYear = reportYear;
        this.totalAmount = totalAmount;
        this.totalFailed = totalFailed;
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

    public Long getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(Long totalFailed) {
        this.totalFailed = totalFailed;
    }

    public static TransactionResponseYearStatusFailed from(TransactionYearStatusFailed dto) {
        return new TransactionResponseYearStatusFailed(
            dto.getYear(),
            dto.getTotalAmount(),
            dto.getTotalFailed()
        );
    }
}
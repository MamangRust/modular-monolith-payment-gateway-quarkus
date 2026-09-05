package com.sanedge.transaction.domain.response.stats.status;

import com.sanedge.transaction.entity.TransactionMonthStatusFailed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseMonthStatusFailed {
    private String reportYear;
    private Long totalAmount;
    private String monthName;
    private Long totalFailed;

    public TransactionResponseMonthStatusFailed(String reportYear, String monthName, long totalAmount, long totalFailed) {
        this.reportYear = reportYear;
        this.monthName = monthName;
        this.totalAmount = totalAmount;
        this.totalFailed = totalFailed;
    }

    public String getReportYear() {
        return reportYear;
    }

    public void setReportYear(String reportYear) {
        this.reportYear = reportYear;
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

    public Long getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(Long totalFailed) {
        this.totalFailed = totalFailed;
    }

    public static TransactionResponseMonthStatusFailed from(TransactionMonthStatusFailed dto) {
        return new TransactionResponseMonthStatusFailed(
            dto.getYear(),
            dto.getMonth(),
            dto.getTotalAmount(),
            dto.getTotalFailed()
        );
    }
}
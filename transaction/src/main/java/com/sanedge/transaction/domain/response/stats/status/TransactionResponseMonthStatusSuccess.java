package com.sanedge.transaction.domain.response.stats.status;

import com.sanedge.transaction.entity.TransactionMonthStatusSuccess;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseMonthStatusSuccess {
    private String reportYear;
    private String monthName;
    private Long totalAmount;
    private Long totalSuccess;

    public TransactionResponseMonthStatusSuccess(String reportYear, String monthName, long totalAmount, long totalSuccess) {
        this.reportYear = reportYear;
        this.monthName = monthName;
        this.totalAmount = totalAmount;
        this.totalSuccess = totalSuccess;
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

    public Long getTotalSuccess() {
        return totalSuccess;
    }

    public void setTotalSuccess(Long totalSuccess) {
        this.totalSuccess = totalSuccess;
    }

    public static TransactionResponseMonthStatusSuccess from(TransactionMonthStatusSuccess dto) {
        return new TransactionResponseMonthStatusSuccess(
            dto.getYear(),
            dto.getMonth(),
            dto.getTotalAmount(),
            dto.getTotalSuccess()
        );
    }
}
package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.TransactionDto;
import io.smallrye.mutiny.Uni;

public interface TransactionService {
    Uni<TransactionDto.ApiResponsePaginationTransaction> listTransactions(int page, int size, String search);
    Uni<TransactionDto.ApiResponsePaginationTransaction> listTransactionsByCard(String cardNumber, int page, int size, String search);
    Uni<TransactionDto.ApiResponsePaginationTransactionDeleteAt> findActiveTransactions(int page, int size, String search);
    Uni<TransactionDto.ApiResponsePaginationTransactionDeleteAt> findTrashedTransactions(int page, int size, String search);
    Uni<TransactionDto.ApiResponseTransaction> getTransaction(int id);
    Uni<TransactionDto.ApiResponseTransactions> findByMerchantId(int merchantId);
    Uni<TransactionDto.ApiResponseTransaction> createTransaction(TransactionDto.CreateRequest body);
    Uni<TransactionDto.ApiResponseTransaction> updateTransaction(int id, TransactionDto.UpdateRequest body);
    Uni<TransactionDto.ApiResponseTransactionDeleteAt> deleteTransaction(int id);
    Uni<TransactionDto.ApiResponseTransactionDeleteAt> trashTransaction(int id);
    Uni<TransactionDto.ApiResponseTransactionDeleteAt> restoreTransaction(int id);
    Uni<TransactionDto.SimpleResponse> deleteTransactionPermanent(int id);
    Uni<TransactionDto.SimpleResponse> restoreAllTransactions();
    Uni<TransactionDto.SimpleResponse> deleteAllTransactions();

    Uni<TransactionDto.ApiResponseTransactionMonthAmount> findMonthlyAmounts(int year);
    Uni<TransactionDto.ApiResponseTransactionYearAmount> findYearlyAmounts(int year);
    Uni<TransactionDto.ApiResponseTransactionMonthAmount> findMonthlyAmountsByCard(int year, String cardNumber);
    Uni<TransactionDto.ApiResponseTransactionYearAmount> findYearlyAmountsByCard(int year, String cardNumber);

    Uni<TransactionDto.ApiResponseTransactionMonthMethod> findMonthlyMethods(int year);
    Uni<TransactionDto.ApiResponseTransactionYearMethod> findYearlyMethods(int year);
    Uni<TransactionDto.ApiResponseTransactionMonthMethod> findMonthlyMethodsByCard(int year, String cardNumber);
    Uni<TransactionDto.ApiResponseTransactionYearMethod> findYearlyMethodsByCard(int year, String cardNumber);

    Uni<TransactionDto.ApiResponseTransactionMonthStatusSuccess> findMonthlyStatusSuccess(int year, int month);
    Uni<TransactionDto.ApiResponseTransactionYearStatusSuccess> findYearlyStatusSuccess(int year);
    Uni<TransactionDto.ApiResponseTransactionMonthStatusFailed> findMonthlyStatusFailed(int year, int month);
    Uni<TransactionDto.ApiResponseTransactionYearStatusFailed> findYearlyStatusFailed(int year);

    Uni<TransactionDto.ApiResponseTransactionMonthStatusSuccess> findMonthlyStatusSuccessByCard(int year, int month, String cardNumber);
    Uni<TransactionDto.ApiResponseTransactionYearStatusSuccess> findYearlyStatusSuccessByCard(int year, String cardNumber);
    Uni<TransactionDto.ApiResponseTransactionMonthStatusFailed> findMonthlyStatusFailedByCard(int year, int month, String cardNumber);
    Uni<TransactionDto.ApiResponseTransactionYearStatusFailed> findYearlyStatusFailedByCard(int year, String cardNumber);
}

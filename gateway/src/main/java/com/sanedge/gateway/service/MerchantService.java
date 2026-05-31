package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.MerchantDto;
import io.smallrye.mutiny.Uni;

public interface MerchantService {
    Uni<MerchantDto.ApiResponsePaginationMerchant> listMerchants(int page, int size, String search);
    Uni<MerchantDto.ApiResponseMerchant> getMerchant(int id);
    Uni<MerchantDto.ApiResponseMerchant> createMerchant(MerchantDto.CreateRequest body);
    Uni<MerchantDto.ApiResponseMerchant> updateMerchant(int id, MerchantDto.UpdateRequest body);
    Uni<MerchantDto.ApiResponseMerchantDeleteAt> deleteMerchant(int id);

    Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findAllTransactions(int page, int size, String search, int merchantId);
    Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findTransactionsById(int page, int size, String search, String id);
    Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findTransactionsByApiKey(int page, int size, String search, String apiKey);

    Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmount(int year);
    Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmount(int year);
    Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmountById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmountById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmountByApiKey(int year, String apiKey);
    Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmountByApiKey(int year, String apiKey);

    Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethod(int year);
    Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethod(int year);
    Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethodById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethodById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethodByApiKey(int year, String apiKey);
    Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethodByApiKey(int year, String apiKey);

    Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmount(int year);
    Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmount(int year);
    Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmountById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmountById(int year, int merchantId);
    Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmountByApiKey(int year, String apiKey);
    Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmountByApiKey(int year, String apiKey);
}

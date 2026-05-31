package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.TopupDto;
import io.smallrye.mutiny.Uni;

public interface TopupService {
    Uni<TopupDto.ApiResponsePaginationTopup> listTopups(int page, int size, String search);
    Uni<TopupDto.ApiResponsePaginationTopup> listTopupsByCard(String cardNumber, int page, int size, String search);
    Uni<TopupDto.ApiResponsePaginationTopupDeleteAt> findActiveTopups(int page, int size, String search);
    Uni<TopupDto.ApiResponsePaginationTopupDeleteAt> findTrashedTopups(int page, int size, String search);
    Uni<TopupDto.ApiResponseTopup> getTopup(int id);
    Uni<TopupDto.ApiResponseTopup> getTopupByCard(String cardNumber, int year);
    Uni<TopupDto.ApiResponseTopup> createTopup(TopupDto.CreateRequest body);
    Uni<TopupDto.ApiResponseTopup> updateTopup(int id, TopupDto.UpdateRequest body);
    Uni<TopupDto.SimpleResponse> deleteTopupPermanent(int id);
    Uni<TopupDto.ApiResponseTopupDeleteAt> trashTopup(int id);
    Uni<TopupDto.ApiResponseTopupDeleteAt> restoreTopup(int id);
    Uni<TopupDto.SimpleResponse> restoreAllTopups();
    Uni<TopupDto.SimpleResponse> deleteAllTopups();

    Uni<TopupDto.ApiResponseTopupMonthAmount> getMonthlyAmounts(int year);
    Uni<TopupDto.ApiResponseTopupYearAmount> getYearlyAmounts(int year);
    Uni<TopupDto.ApiResponseTopupMonthAmount> getMonthlyAmountsByCard(int year, String cardNumber);
    Uni<TopupDto.ApiResponseTopupYearAmount> getYearlyAmountsByCard(int year, String cardNumber);

    Uni<TopupDto.ApiResponseTopupMonthMethod> getMonthlyMethods(int year);
    Uni<TopupDto.ApiResponseTopupYearMethod> getYearlyMethods(int year);
    Uni<TopupDto.ApiResponseTopupMonthMethod> getMonthlyMethodsByCard(int year, String cardNumber);
    Uni<TopupDto.ApiResponseTopupYearMethod> getYearlyMethodsByCard(int year, String cardNumber);

    Uni<TopupDto.ApiResponseTopupMonthStatusSuccess> getMonthlyStatusSuccess(int year, int month);
    Uni<TopupDto.ApiResponseTopupYearStatusSuccess> getYearlyStatusSuccess(int year);
    Uni<TopupDto.ApiResponseTopupMonthStatusFailed> getMonthlyStatusFailed(int year, int month);
    Uni<TopupDto.ApiResponseTopupYearStatusFailed> getYearlyStatusFailed(int year);

    Uni<TopupDto.ApiResponseTopupMonthStatusSuccess> getMonthlyStatusSuccessByCard(int year, int month, String cardNumber);
    Uni<TopupDto.ApiResponseTopupYearStatusSuccess> getYearlyStatusSuccessByCard(int year, String cardNumber);
    Uni<TopupDto.ApiResponseTopupMonthStatusFailed> getMonthlyStatusFailedByCard(int year, int month, String cardNumber);
    Uni<TopupDto.ApiResponseTopupYearStatusFailed> getYearlyStatusFailedByCard(int year, String cardNumber);
}

package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.WithdrawDto;
import io.smallrye.mutiny.Uni;

public interface WithdrawService {
    Uni<WithdrawDto.ApiResponsePaginationWithdraw> listWithdraws(int page, int size, String search);
    Uni<WithdrawDto.ApiResponsesWithdraw> findByCard(String cardNumber);
    Uni<WithdrawDto.ApiResponsePaginationWithdrawDeleteAt> findActiveWithdraws(int page, int size, String search);
    Uni<WithdrawDto.ApiResponsePaginationWithdrawDeleteAt> findTrashedWithdraws(int page, int size, String search);
    Uni<WithdrawDto.ApiResponseWithdraw> getWithdraw(int id);
    Uni<WithdrawDto.ApiResponseWithdraw> createWithdraw(WithdrawDto.CreateRequest body);
    Uni<WithdrawDto.ApiResponseWithdraw> updateWithdraw(int id, WithdrawDto.UpdateRequest body);
    Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> deleteWithdraw(int id);
    Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> trashWithdraw(int id);
    Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> restoreWithdraw(int id);
    Uni<WithdrawDto.SimpleResponse> deleteWithdrawPermanent(int id);
    Uni<WithdrawDto.SimpleResponse> restoreAllWithdraws();
    Uni<WithdrawDto.SimpleResponse> deleteAllWithdraws();

    Uni<WithdrawDto.ApiResponseWithdrawMonthAmount> findMonthlyAmounts(int year);
    Uni<WithdrawDto.ApiResponseWithdrawYearAmount> findYearlyAmounts(int year);
    Uni<WithdrawDto.ApiResponseWithdrawMonthAmount> findMonthlyByCard(int year, String cardNumber);
    Uni<WithdrawDto.ApiResponseWithdrawYearAmount> findYearlyByCard(int year, String cardNumber);

    Uni<WithdrawDto.ApiResponseWithdrawMonthStatusSuccess> findMonthlyStatusSuccess(int year, int month);
    Uni<WithdrawDto.ApiResponseWithdrawYearStatusSuccess> findYearlyStatusSuccess(int year);
    Uni<WithdrawDto.ApiResponseWithdrawMonthStatusFailed> findMonthlyStatusFailed(int year, int month);
    Uni<WithdrawDto.ApiResponseWithdrawYearStatusFailed> findYearlyStatusFailed(int year);

    Uni<WithdrawDto.ApiResponseWithdrawMonthStatusSuccess> findMonthlyStatusSuccessByCard(int year, int month, String cardNumber);
    Uni<WithdrawDto.ApiResponseWithdrawYearStatusSuccess> findYearlyStatusSuccessByCard(int year, String cardNumber);
    Uni<WithdrawDto.ApiResponseWithdrawMonthStatusFailed> findMonthlyStatusFailedByCard(int year, int month, String cardNumber);
    Uni<WithdrawDto.ApiResponseWithdrawYearStatusFailed> findYearlyStatusFailedByCard(int year, String cardNumber);
}

package com.sanedge.gateway.service;

import com.sanedge.gateway.dto.CardDto;
import io.smallrye.mutiny.Uni;

public interface CardService {
    Uni<CardDto.ApiResponsePaginationCard> listCards(int page, int size, String search);
    Uni<CardDto.ApiResponsePaginationCardDeleteAt> findActiveCards(int page, int size, String search);
    Uni<CardDto.ApiResponsePaginationCardDeleteAt> findTrashedCards(int page, int size, String search);
    Uni<CardDto.ApiResponseCard> getCard(int id);
    Uni<CardDto.ApiResponseCard> findCardByUser(int userId);
    Uni<CardDto.ApiResponseCard> findCardByNumber(String cardNumber);
    Uni<CardDto.ApiResponseCard> createCard(CardDto.CreateRequest body);
    Uni<CardDto.ApiResponseCard> updateCard(int id, CardDto.UpdateRequest body);
    Uni<CardDto.ApiResponseCardDeleteAt> deleteCard(int id);
    Uni<CardDto.SimpleResponse> deleteCardPermanent(int id);
    Uni<CardDto.ApiResponseCardDeleteAt> restoreCard(int id);
    Uni<CardDto.SimpleResponse> restoreAllCards();
    Uni<CardDto.SimpleResponse> deleteAllCards();

    Uni<CardDto.ApiResponseMonthlyBalance> findMonthlyBalance(int year);
    Uni<CardDto.ApiResponseYearlyBalance> findYearlyBalance(int year);
    Uni<CardDto.ApiResponseMonthlyBalance> getMonthlyBalanceByCard(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyBalance> getYearlyBalanceByCard(int year, String cardNumber);

    Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTopupAmount(int year);
    Uni<CardDto.ApiResponseYearlyAmount> findYearlyTopupAmount(int year);
    Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTopupAmountByCard(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyAmount> getYearlyTopupAmountByCard(int year, String cardNumber);

    Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransactionAmount(int year);
    Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransactionAmount(int year);
    Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransactionAmountByCard(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransactionAmountByCard(int year, String cardNumber);

    Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransferAmountSender(int year);
    Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransferAmountReceiver(int year);
    Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransferAmountSender(int year);
    Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransferAmountReceiver(int year);
    Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransferAmountByCardSender(int year, String cardNumber);
    Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransferAmountByCardReceiver(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransferAmountByCardSender(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransferAmountByCardReceiver(int year, String cardNumber);

    Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyWithdrawAmount(int year);
    Uni<CardDto.ApiResponseYearlyAmount> findYearlyWithdrawAmount(int year);
    Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyWithdrawAmountByCard(int year, String cardNumber);
    Uni<CardDto.ApiResponseYearlyAmount> getYearlyWithdrawAmountByCard(int year, String cardNumber);

    Uni<CardDto.ApiResponseDashboardCard> findCardDashboard();
    Uni<CardDto.ApiResponseDashboardCardNumber> findCardDashboardByCardNumber(String cardNumber);
}

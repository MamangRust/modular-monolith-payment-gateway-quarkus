package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.CardDto;
import com.sanedge.gateway.service.CardService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CardServiceImpl implements CardService {

    private static final Logger LOG = Logger.getLogger(CardServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("card")
    pb.card.MutinyCardQueryServiceGrpc.MutinyCardQueryServiceStub cardQueryService;

    @GrpcClient("card")
    pb.card.MutinyCardCommandServiceGrpc.MutinyCardCommandServiceStub cardCommandService;

    @GrpcClient("card")
    pb.card.MutinyCardDashboardServiceGrpc.MutinyCardDashboardServiceStub cardDashboardService;

    @GrpcClient("card")
    pb.card.stats.MutinyCardStatsBalanceServiceGrpc.MutinyCardStatsBalanceServiceStub cardStatsBalanceService;

    @GrpcClient("card")
    pb.card.stats.MutinyCardStatsTopupServiceGrpc.MutinyCardStatsTopupServiceStub cardStatsTopupService;

    @GrpcClient("card")
    pb.card.stats.MutinyCardStatsTransactionServiceGrpc.MutinyCardStatsTransactionServiceStub cardStatsTransactionService;

    @GrpcClient("card")
    pb.card.stats.MutinyCardStatsTransferServiceGrpc.MutinyCardStatsTransferServiceStub cardStatsTransferService;

    @GrpcClient("card")
    pb.card.stats.MutinyCardStatsWithdrawServiceGrpc.MutinyCardStatsWithdrawServiceStub cardStatsWithdrawService;

    @Override
    public Uni<CardDto.ApiResponsePaginationCard> listCards(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("card.listCards", () -> cardQueryService.findAllCard(pb.card.Card.FindAllCardRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(CardDto.ApiResponsePaginationCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list cards: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponsePaginationCardDeleteAt> findActiveCards(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("card.findActiveCards", () -> cardQueryService.findByActiveCard(pb.card.Card.FindAllCardRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(CardDto.ApiResponsePaginationCardDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find active cards: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponsePaginationCardDeleteAt> findTrashedCards(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("card.findTrashedCards", () -> cardQueryService.findByTrashedCard(pb.card.Card.FindAllCardRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(CardDto.ApiResponsePaginationCardDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find trashed cards: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCard> getCard(int id) {
        return telemetryHelper.traceAndMetric("card.getCard", () -> cardQueryService.findByIdCard(pb.card.Card.FindByIdCardRequest.newBuilder()
                .setCardId(id)
                .build())
                .map(CardDto.ApiResponseCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get card with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCard> findCardByUser(int userId) {
        return telemetryHelper.traceAndMetric("card.findCardByUser", () -> cardQueryService.findByUserIdCard(pb.card.Card.FindByUserIdCardRequest.newBuilder()
                .setUserId(userId)
                .build())
                .map(CardDto.ApiResponseCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get card for user " + userId + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCard> findCardByNumber(String cardNumber) {
        return telemetryHelper.traceAndMetric("card.findCardByNumber", () -> cardQueryService.findByCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find card by number: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCard> createCard(CardDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("card.createCard", () -> cardCommandService.createCard(pb.card.CardCommand.CreateCardRequest.newBuilder()
                .setUserId(body.userId())
                .setCardType(body.cardType())
                .setExpireDate(CardDto.toProtoTimestamp(body.expireDate()))
                .setCvv(body.cvv())
                .setCardProvider(body.cardProvider())
                .build())
                .map(CardDto.ApiResponseCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCard> updateCard(int id, CardDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("card.updateCard", () -> cardCommandService.updateCard(pb.card.CardCommand.UpdateCardRequest.newBuilder()
                .setCardId(id)
                .setUserId(body.userId())
                .setCardType(body.cardType())
                .setExpireDate(CardDto.toProtoTimestamp(body.expireDate()))
                .setCvv(body.cvv())
                .setCardProvider(body.cardProvider())
                .build())
                .map(CardDto.ApiResponseCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCardDeleteAt> deleteCard(int id) {
        return telemetryHelper.traceAndMetric("card.deleteCard", () -> cardCommandService.trashedCard(pb.card.Card.FindByIdCardRequest.newBuilder()
                .setCardId(id)
                .build())
                .map(CardDto.ApiResponseCardDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.SimpleResponse> deleteCardPermanent(int id) {
        return telemetryHelper.traceAndMetric("card.deleteCardPermanent", () -> cardCommandService.deleteCardPermanent(pb.card.Card.FindByIdCardRequest.newBuilder()
                .setCardId(id)
                .build())
                .map(CardDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to delete card permanently: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseCardDeleteAt> restoreCard(int id) {
        return telemetryHelper.traceAndMetric("card.restoreCard", () -> cardCommandService.restoreCard(pb.card.Card.FindByIdCardRequest.newBuilder()
                .setCardId(id)
                .build())
                .map(CardDto.ApiResponseCardDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.SimpleResponse> restoreAllCards() {
        return telemetryHelper.traceAndMetric("card.restoreAllCards", () -> cardCommandService.restoreAllCard(com.google.protobuf.Empty.getDefaultInstance())
                .map(CardDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore all cards: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.SimpleResponse> deleteAllCards() {
        return telemetryHelper.traceAndMetric("card.deleteAllCards", () -> cardCommandService.deleteAllCardPermanent(com.google.protobuf.Empty.getDefaultInstance())
                .map(CardDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete all cards: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyBalance> findMonthlyBalance(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyBalance", () -> cardStatsBalanceService.findMonthlyBalance(pb.card.stats.CardStatsBalance.FindYearBalance.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyBalance::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly balance stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyBalance> findYearlyBalance(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyBalance", () -> cardStatsBalanceService.findYearlyBalance(pb.card.stats.CardStatsBalance.FindYearBalance.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyBalance::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly balance stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyBalance> getMonthlyBalanceByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyBalanceByCard", () -> cardStatsBalanceService.findMonthlyBalanceByCardNumber(pb.card.stats.CardStatsBalance.FindYearBalanceCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyBalance::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly balance by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyBalance> getYearlyBalanceByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyBalanceByCard", () -> cardStatsBalanceService.findYearlyBalanceByCardNumber(pb.card.stats.CardStatsBalance.FindYearBalanceCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyBalance::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly balance by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTopupAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyTopupAmount", () -> cardStatsTopupService.findMonthlyTopupAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> findYearlyTopupAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyTopupAmount", () -> cardStatsTopupService.findYearlyTopupAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTopupAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyTopupAmountByCard", () -> cardStatsTopupService.findMonthlyTopupAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> getYearlyTopupAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyTopupAmountByCard", () -> cardStatsTopupService.findYearlyTopupAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransactionAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyTransactionAmount", () -> cardStatsTransactionService.findMonthlyTransactionAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransactionAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyTransactionAmount", () -> cardStatsTransactionService.findYearlyTransactionAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransactionAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyTransactionAmountByCard", () -> cardStatsTransactionService.findMonthlyTransactionAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransactionAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyTransactionAmountByCard", () -> cardStatsTransactionService.findYearlyTransactionAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransferAmountSender(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyTransferAmountSender", () -> cardStatsTransferService.findMonthlyTransferSenderAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer sender: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyTransferAmountReceiver(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyTransferAmountReceiver", () -> cardStatsTransferService.findMonthlyTransferReceiverAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer receiver: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransferAmountSender(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyTransferAmountSender", () -> cardStatsTransferService.findYearlyTransferSenderAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer sender: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> findYearlyTransferAmountReceiver(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyTransferAmountReceiver", () -> cardStatsTransferService.findYearlyTransferReceiverAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer receiver: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransferAmountByCardSender(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyTransferAmountByCardSender", () -> cardStatsTransferService.findMonthlyTransferSenderAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer sender by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyTransferAmountByCardReceiver(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyTransferAmountByCardReceiver", () -> cardStatsTransferService.findMonthlyTransferReceiverAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer receiver by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransferAmountByCardSender(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyTransferAmountByCardSender", () -> cardStatsTransferService.findYearlyTransferSenderAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer sender by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> getYearlyTransferAmountByCardReceiver(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyTransferAmountByCardReceiver", () -> cardStatsTransferService.findYearlyTransferReceiverAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer receiver by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> findMonthlyWithdrawAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findMonthlyWithdrawAmount", () -> cardStatsWithdrawService.findMonthlyWithdrawAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> findYearlyWithdrawAmount(int year) {
        return telemetryHelper.traceAndMetric("card.findYearlyWithdrawAmount", () -> cardStatsWithdrawService.findYearlyWithdrawAmount(pb.card.Card.FindYearAmount.newBuilder()
                .setYear(year)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw amount: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseMonthlyAmount> getMonthlyWithdrawAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getMonthlyWithdrawAmountByCard", () -> cardStatsWithdrawService.findMonthlyWithdrawAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseYearlyAmount> getYearlyWithdrawAmountByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("card.getYearlyWithdrawAmountByCard", () -> cardStatsWithdrawService.findYearlyWithdrawAmountByCardNumber(pb.card.Card.FindYearAmountCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseDashboardCard> findCardDashboard() {
        return telemetryHelper.traceAndMetric("card.findCardDashboard", () -> cardDashboardService.dashboardCard(com.google.protobuf.Empty.getDefaultInstance())
                .map(CardDto.ApiResponseDashboardCard::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get card dashboard stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<CardDto.ApiResponseDashboardCardNumber> findCardDashboardByCardNumber(String cardNumber) {
        return telemetryHelper.traceAndMetric("card.findCardDashboardByCardNumber", () -> cardDashboardService.dashboardCardNumber(pb.card.Card.FindByCardNumberRequest.newBuilder()
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(CardDto.ApiResponseDashboardCardNumber::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get card dashboard by number: " + throwable.getMessage(), throwable)));
    }
}

package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.TransactionDto;
import com.sanedge.gateway.service.TransactionService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = Logger.getLogger(TransactionServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("transaction")
    pb.transaction.MutinyTransactionQueryServiceGrpc.MutinyTransactionQueryServiceStub transactionQueryService;

    @GrpcClient("transaction")
    pb.transaction.MutinyTransactionCommandServiceGrpc.MutinyTransactionCommandServiceStub transactionCommandService;

    @GrpcClient("transaction")
    pb.transaction.stats.MutinyTransactionStatsAmountServiceGrpc.MutinyTransactionStatsAmountServiceStub transactionStatsAmountService;

    @GrpcClient("transaction")
    pb.transaction.stats.MutinyTransactionStatsMethodServiceGrpc.MutinyTransactionStatsMethodServiceStub transactionStatsMethodService;

    @GrpcClient("transaction")
    pb.transaction.stats.MutinyTransactionStatsStatusServiceGrpc.MutinyTransactionStatsStatusServiceStub transactionStatsStatusService;

    @Override
    public Uni<TransactionDto.ApiResponsePaginationTransaction> listTransactions(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transaction.listTransactions", () -> transactionQueryService.findAllTransaction(
                pb.transaction.TransactionQuery.FindAllTransactionRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransactionDto.ApiResponsePaginationTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponsePaginationTransaction> listTransactionsByCard(String cardNumber, int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transaction.listTransactionsByCard", () -> transactionQueryService.findAllTransactionByCardNumber(
                pb.transaction.TransactionQuery.FindAllTransactionCardNumberRequest.newBuilder()
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransactionDto.ApiResponsePaginationTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list transactions by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponsePaginationTransactionDeleteAt> findActiveTransactions(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transaction.findActiveTransactions", () -> transactionQueryService.findByActiveTransaction(
                pb.transaction.TransactionQuery.FindAllTransactionRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransactionDto.ApiResponsePaginationTransactionDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find active transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponsePaginationTransactionDeleteAt> findTrashedTransactions(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transaction.findTrashedTransactions", () -> transactionQueryService.findByTrashedTransaction(
                pb.transaction.TransactionQuery.FindAllTransactionRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransactionDto.ApiResponsePaginationTransactionDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find trashed transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransaction> getTransaction(int id) {
        return telemetryHelper.traceAndMetric("transaction.getTransaction", () -> transactionQueryService.findByIdTransaction(
                pb.transaction.Transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build())
                .map(TransactionDto.ApiResponseTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get transaction with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactions> findByMerchantId(int merchantId) {
        return telemetryHelper.traceAndMetric("transaction.findByMerchantId", () -> transactionQueryService.findTransactionByMerchantId(
                pb.transaction.TransactionQuery.FindTransactionByMerchantIdRequest.newBuilder()
                        .setMerchantId(merchantId)
                        .build())
                .map(TransactionDto.ApiResponseTransactions::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get transactions by merchant id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransaction> createTransaction(TransactionDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("transaction.createTransaction", () -> transactionCommandService.createTransaction(
                pb.transaction.TransactionCommand.CreateTransactionRequest.newBuilder()
                        .setApiKey(body.apiKey() == null ? "" : body.apiKey())
                        .setCardNumber(body.cardNumber() == null ? "" : body.cardNumber())
                        .setAmount(body.amount())
                        .setPaymentMethod(body.paymentMethod() == null ? "" : body.paymentMethod())
                        .setMerchantId(body.merchantId())
                        .setTransactionTime(com.google.protobuf.Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000)
                                .build())
                        .build())
                .map(TransactionDto.ApiResponseTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransaction> updateTransaction(int id, TransactionDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("transaction.updateTransaction", () -> transactionCommandService.updateTransaction(
                pb.transaction.TransactionCommand.UpdateTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .setApiKey(body.apiKey() == null ? "" : body.apiKey())
                        .setCardNumber(body.cardNumber() == null ? "" : body.cardNumber())
                        .setAmount(body.amount())
                        .setPaymentMethod(body.paymentMethod() == null ? "" : body.paymentMethod())
                        .setMerchantId(body.merchantId())
                        .setTransactionTime(com.google.protobuf.Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000)
                                .build())
                        .build())
                .map(TransactionDto.ApiResponseTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionDeleteAt> deleteTransaction(int id) {
        return telemetryHelper.traceAndMetric("transaction.deleteTransaction", () -> transactionCommandService.trashedTransaction(
                pb.transaction.Transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build())
                .map(TransactionDto.ApiResponseTransactionDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionDeleteAt> trashTransaction(int id) {
        return telemetryHelper.traceAndMetric("transaction.trashTransaction", () -> transactionCommandService.trashedTransaction(
                pb.transaction.Transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build())
                .map(TransactionDto.ApiResponseTransactionDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to trash transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionDeleteAt> restoreTransaction(int id) {
        return telemetryHelper.traceAndMetric("transaction.restoreTransaction", () -> transactionCommandService.restoreTransaction(
                pb.transaction.Transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build())
                .map(TransactionDto.ApiResponseTransactionDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.SimpleResponse> deleteTransactionPermanent(int id) {
        return telemetryHelper.traceAndMetric("transaction.deleteTransactionPermanent", () -> transactionCommandService.deleteTransactionPermanent(
                pb.transaction.Transaction.FindByIdTransactionRequest.newBuilder()
                        .setTransactionId(id)
                        .build())
                .map(TransactionDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete transaction: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.SimpleResponse> restoreAllTransactions() {
        return telemetryHelper.traceAndMetric("transaction.restoreAllTransactions", () -> transactionCommandService.restoreAllTransaction(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(TransactionDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore all transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.SimpleResponse> deleteAllTransactions() {
        return telemetryHelper.traceAndMetric("transaction.deleteAllTransactions", () -> transactionCommandService.deleteAllTransactionPermanent(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(TransactionDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete all transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthAmount> findMonthlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyAmounts", () -> transactionStatsAmountService.findMonthlyAmounts(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearAmount> findYearlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyAmounts", () -> transactionStatsAmountService.findYearlyAmounts(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthAmount> findMonthlyAmountsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyAmountsByCard", () -> transactionStatsAmountService.findMonthlyAmountsByCardNumber(
                pb.transaction.Transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearAmount> findYearlyAmountsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyAmountsByCard", () -> transactionStatsAmountService.findYearlyAmountsByCardNumber(
                pb.transaction.Transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthMethod> findMonthlyMethods(int year) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyMethods", () -> transactionStatsMethodService.findMonthlyPaymentMethods(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction methods: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearMethod> findYearlyMethods(int year) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyMethods", () -> transactionStatsMethodService.findYearlyPaymentMethods(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction methods: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthMethod> findMonthlyMethodsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyMethodsByCard", () -> transactionStatsMethodService.findMonthlyPaymentMethodsByCardNumber(
                pb.transaction.Transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction methods by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearMethod> findYearlyMethodsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyMethodsByCard", () -> transactionStatsMethodService.findYearlyPaymentMethodsByCardNumber(
                pb.transaction.Transaction.FindByYearCardNumberTransactionRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction methods by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthStatusSuccess> findMonthlyStatusSuccess(int year, int month) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyStatusSuccess", () -> transactionStatsStatusService.findMonthlyTransactionStatusSuccess(
                pb.transaction.Transaction.FindMonthlyTransactionStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearStatusSuccess> findYearlyStatusSuccess(int year) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyStatusSuccess", () -> transactionStatsStatusService.findYearlyTransactionStatusSuccess(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthStatusFailed> findMonthlyStatusFailed(int year, int month) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyStatusFailed", () -> transactionStatsStatusService.findMonthlyTransactionStatusFailed(
                pb.transaction.Transaction.FindMonthlyTransactionStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearStatusFailed> findYearlyStatusFailed(int year) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyStatusFailed", () -> transactionStatsStatusService.findYearlyTransactionStatusFailed(
                pb.transaction.Transaction.FindYearTransactionStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthStatusSuccess> findMonthlyStatusSuccessByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyStatusSuccessByCard", () -> transactionStatsStatusService.findMonthlyTransactionStatusSuccessByCardNumber(
                pb.transaction.Transaction.FindMonthlyTransactionStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearStatusSuccess> findYearlyStatusSuccessByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyStatusSuccessByCard", () -> transactionStatsStatusService.findYearlyTransactionStatusSuccessByCardNumber(
                pb.transaction.Transaction.FindYearTransactionStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionMonthStatusFailed> findMonthlyStatusFailedByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findMonthlyStatusFailedByCard", () -> transactionStatsStatusService.findMonthlyTransactionStatusFailedByCardNumber(
                pb.transaction.Transaction.FindMonthlyTransactionStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transaction failed by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransactionDto.ApiResponseTransactionYearStatusFailed> findYearlyStatusFailedByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transaction.findYearlyStatusFailedByCard", () -> transactionStatsStatusService.findYearlyTransactionStatusFailedByCardNumber(
                pb.transaction.Transaction.FindYearTransactionStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransactionDto.ApiResponseTransactionYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transaction failed by card: " + throwable.getMessage(), throwable)));
    }
}

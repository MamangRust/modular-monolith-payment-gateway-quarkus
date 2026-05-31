package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.TransferDto;
import com.sanedge.gateway.service.TransferService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransferServiceImpl implements TransferService {

    private static final Logger LOG = Logger.getLogger(TransferServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("transfer")
    pb.transfer.MutinyTransferQueryServiceGrpc.MutinyTransferQueryServiceStub transferQueryService;

    @GrpcClient("transfer")
    pb.transfer.MutinyTransferCommandServiceGrpc.MutinyTransferCommandServiceStub transferCommandService;

    @GrpcClient("transfer")
    pb.transfer.stats.MutinyTransferStatsAmountServiceGrpc.MutinyTransferStatsAmountServiceStub transferStatsAmountService;

    @GrpcClient("transfer")
    pb.transfer.stats.MutinyTransferStatsStatusServiceGrpc.MutinyTransferStatsStatusServiceStub transferStatsStatusService;

    @Override
    public Uni<TransferDto.ApiResponsePaginationTransfer> listTransfers(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transfer.listTransfers", () -> transferQueryService.findAllTransfer(
                pb.transfer.Transfer.FindAllTransferRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransferDto.ApiResponsePaginationTransfer::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list transfers: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponsePaginationTransferDeleteAt> findActiveTransfers(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transfer.findActiveTransfers", () -> transferQueryService.findByActiveTransfer(
                pb.transfer.Transfer.FindAllTransferRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransferDto.ApiResponsePaginationTransferDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find active transfers: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponsePaginationTransferDeleteAt> findTrashedTransfers(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("transfer.findTrashedTransfers", () -> transferQueryService.findByTrashedTransfer(
                pb.transfer.Transfer.FindAllTransferRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(TransferDto.ApiResponsePaginationTransferDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find trashed transfers: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransfer> getTransfer(int id) {
        return telemetryHelper.traceAndMetric("transfer.getTransfer", () -> transferQueryService.findByIdTransfer(
                pb.transfer.Transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id)
                        .build())
                .map(TransferDto.ApiResponseTransfer::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get transfer with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransfers> findTransfersFrom(String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findTransfersFrom", () -> transferQueryService.findTransferByTransferFrom(
                pb.transfer.Transfer.FindTransferByTransferFromRequest.newBuilder()
                        .setTransferFrom(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransfers::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get transfers from card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransfers> findTransfersTo(String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findTransfersTo", () -> transferQueryService.findTransferByTransferTo(
                pb.transfer.Transfer.FindTransferByTransferToRequest.newBuilder()
                        .setTransferTo(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransfers::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get transfers to card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransfer> createTransfer(TransferDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("transfer.createTransfer", () -> transferCommandService.createTransfer(
                pb.transfer.TransferCommand.CreateTransferRequest.newBuilder()
                        .setTransferFrom(body.transferFrom() == null ? "" : body.transferFrom())
                        .setTransferTo(body.transferTo() == null ? "" : body.transferTo())
                        .setTransferAmount(body.transferAmount())
                        .build())
                .map(TransferDto.ApiResponseTransfer::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransfer> updateTransfer(int id, TransferDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("transfer.updateTransfer", () -> transferCommandService.updateTransfer(
                pb.transfer.TransferCommand.UpdateTransferRequest.newBuilder()
                        .setTransferId(id)
                        .setTransferFrom(body.transferFrom() == null ? "" : body.transferFrom())
                        .setTransferTo(body.transferTo() == null ? "" : body.transferTo())
                        .setTransferAmount(body.transferAmount())
                        .build())
                .map(TransferDto.ApiResponseTransfer::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferDeleteAt> deleteTransfer(int id) {
        return telemetryHelper.traceAndMetric("transfer.deleteTransfer", () -> transferCommandService.trashedTransfer(
                pb.transfer.Transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id)
                        .build())
                .map(TransferDto.ApiResponseTransferDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferDeleteAt> trashTransfer(int id) {
        return telemetryHelper.traceAndMetric("transfer.trashTransfer", () -> transferCommandService.trashedTransfer(
                pb.transfer.Transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id)
                        .build())
                .map(TransferDto.ApiResponseTransferDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to trash transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferDeleteAt> restoreTransfer(int id) {
        return telemetryHelper.traceAndMetric("transfer.restoreTransfer", () -> transferCommandService.restoreTransfer(
                pb.transfer.Transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id)
                        .build())
                .map(TransferDto.ApiResponseTransferDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.SimpleResponse> deleteTransferPermanent(int id) {
        return telemetryHelper.traceAndMetric("transfer.deleteTransferPermanent", () -> transferCommandService.deleteTransferPermanent(
                pb.transfer.Transfer.FindByIdTransferRequest.newBuilder()
                        .setTransferId(id)
                        .build())
                .map(TransferDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete transfer: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.SimpleResponse> restoreAllTransfers() {
        return telemetryHelper.traceAndMetric("transfer.restoreAllTransfers", () -> transferCommandService.restoreAllTransfer(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(TransferDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore all transfers: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.SimpleResponse> deleteAllTransfers() {
        return telemetryHelper.traceAndMetric("transfer.deleteAllTransfers", () -> transferCommandService.deleteAllTransferPermanent(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(TransferDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete all transfers: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthAmount> findMonthlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyAmounts", () -> transferStatsAmountService.findMonthlyTransferAmounts(
                pb.transfer.Transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearAmount> findYearlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyAmounts", () -> transferStatsAmountService.findYearlyTransferAmounts(
                pb.transfer.Transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransferDto.ApiResponseTransferYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthAmount> findMonthlyAmountsFromCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyAmountsFromCard", () -> transferStatsAmountService.findMonthlyTransferAmountsBySenderCardNumber(
                pb.transfer.Transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer amounts from card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthAmount> findMonthlyAmountsToCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyAmountsToCard", () -> transferStatsAmountService.findMonthlyTransferAmountsByReceiverCardNumber(
                pb.transfer.Transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer amounts to card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearAmount> findYearlyAmountsFromCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyAmountsFromCard", () -> transferStatsAmountService.findYearlyTransferAmountsBySenderCardNumber(
                pb.transfer.Transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer amounts from card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearAmount> findYearlyAmountsToCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyAmountsToCard", () -> transferStatsAmountService.findYearlyTransferAmountsByReceiverCardNumber(
                pb.transfer.Transfer.FindByCardNumberTransferRequest.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer amounts to card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthStatusSuccess> findMonthlyStatusSuccess(int year, int month) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyStatusSuccess", () -> transferStatsStatusService.findMonthlyTransferStatusSuccess(
                pb.transfer.Transfer.FindMonthlyTransferStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearStatusSuccess> findYearlyStatusSuccess(int year) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyStatusSuccess", () -> transferStatsStatusService.findYearlyTransferStatusSuccess(
                pb.transfer.Transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransferDto.ApiResponseTransferYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthStatusFailed> findMonthlyStatusFailed(int year, int month) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyStatusFailed", () -> transferStatsStatusService.findMonthlyTransferStatusFailed(
                pb.transfer.Transfer.FindMonthlyTransferStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearStatusFailed> findYearlyStatusFailed(int year) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyStatusFailed", () -> transferStatsStatusService.findYearlyTransferStatusFailed(
                pb.transfer.Transfer.FindYearTransferStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(TransferDto.ApiResponseTransferYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthStatusSuccess> findMonthlyStatusSuccessByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyStatusSuccessByCard", () -> transferStatsStatusService.findMonthlyTransferStatusSuccessByCardNumber(
                pb.transfer.Transfer.FindMonthlyTransferStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearStatusSuccess> findYearlyStatusSuccessByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyStatusSuccessByCard", () -> transferStatsStatusService.findYearlyTransferStatusSuccessByCardNumber(
                pb.transfer.Transfer.FindYearTransferStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferMonthStatusFailed> findMonthlyStatusFailedByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findMonthlyStatusFailedByCard", () -> transferStatsStatusService.findMonthlyTransferStatusFailedByCardNumber(
                pb.transfer.Transfer.FindMonthlyTransferStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly transfer failed by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TransferDto.ApiResponseTransferYearStatusFailed> findYearlyStatusFailedByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("transfer.findYearlyStatusFailedByCard", () -> transferStatsStatusService.findYearlyTransferStatusFailedByCardNumber(
                pb.transfer.Transfer.FindYearTransferStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(TransferDto.ApiResponseTransferYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly transfer failed by card: " + throwable.getMessage(), throwable)));
    }
}

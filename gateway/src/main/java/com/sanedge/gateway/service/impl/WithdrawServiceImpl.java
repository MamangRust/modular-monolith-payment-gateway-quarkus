package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.WithdrawDto;
import com.sanedge.gateway.service.WithdrawService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WithdrawServiceImpl implements WithdrawService {

    private static final Logger LOG = Logger.getLogger(WithdrawServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("withdraw")
    pb.withdraw.MutinyWithdrawQueryServiceGrpc.MutinyWithdrawQueryServiceStub withdrawQueryService;

    @GrpcClient("withdraw")
    pb.withdraw.MutinyWithdrawCommandServiceGrpc.MutinyWithdrawCommandServiceStub withdrawCommandService;

    @GrpcClient("withdraw")
    pb.withdraw.stats.MutinyWithdrawStatsAmountServiceGrpc.MutinyWithdrawStatsAmountServiceStub withdrawStatsAmountService;

    @GrpcClient("withdraw")
    pb.withdraw.stats.MutinyWithdrawStatsStatusServiceGrpc.MutinyWithdrawStatsStatusServiceStub withdrawStatsStatusService;

    @Override
    public Uni<WithdrawDto.ApiResponsePaginationWithdraw> listWithdraws(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("withdraw.listWithdraws", () -> withdrawQueryService.findAllWithdraw(
                pb.withdraw.Withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(WithdrawDto.ApiResponsePaginationWithdraw::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list withdraws: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponsesWithdraw> findByCard(String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findByCard", () -> withdrawQueryService.findByCardNumber(
                pb.card.Card.FindByCardNumberRequest.newBuilder()
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponsesWithdraw::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get withdraws by card number: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponsePaginationWithdrawDeleteAt> findActiveWithdraws(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("withdraw.findActiveWithdraws", () -> withdrawQueryService.findByActive(
                pb.withdraw.Withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(WithdrawDto.ApiResponsePaginationWithdrawDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find active withdraws: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponsePaginationWithdrawDeleteAt> findTrashedWithdraws(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("withdraw.findTrashedWithdraws", () -> withdrawQueryService.findByTrashed(
                pb.withdraw.Withdraw.FindAllWithdrawRequest.newBuilder()
                        .setPage(page)
                        .setPageSize(size)
                        .setSearch(search == null ? "" : search)
                        .build())
                .map(WithdrawDto.ApiResponsePaginationWithdrawDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find trashed withdraws: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdraw> getWithdraw(int id) {
        return telemetryHelper.traceAndMetric("withdraw.getWithdraw", () -> withdrawQueryService.findByIdWithdraw(
                pb.withdraw.Withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .build())
                .map(WithdrawDto.ApiResponseWithdraw::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get withdraw with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdraw> createWithdraw(WithdrawDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("withdraw.createWithdraw", () -> withdrawCommandService.createWithdraw(
                pb.withdraw.WithdrawCommand.CreateWithdrawRequest.newBuilder()
                        .setCardNumber(body.cardNumber() == null ? "" : body.cardNumber())
                        .setWithdrawAmount(body.withdrawAmount())
                        .setWithdrawTime(com.google.protobuf.Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000)
                                .build())
                        .build())
                .map(WithdrawDto.ApiResponseWithdraw::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdraw> updateWithdraw(int id, WithdrawDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("withdraw.updateWithdraw", () -> withdrawCommandService.updateWithdraw(
                pb.withdraw.WithdrawCommand.UpdateWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .setCardNumber(body.cardNumber() == null ? "" : body.cardNumber())
                        .setWithdrawAmount(body.withdrawAmount())
                        .setWithdrawTime(com.google.protobuf.Timestamp.newBuilder()
                                .setSeconds(System.currentTimeMillis() / 1000)
                                .build())
                        .build())
                .map(WithdrawDto.ApiResponseWithdraw::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> deleteWithdraw(int id) {
        return telemetryHelper.traceAndMetric("withdraw.deleteWithdraw", () -> withdrawCommandService.trashedWithdraw(
                pb.withdraw.Withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> trashWithdraw(int id) {
        return telemetryHelper.traceAndMetric("withdraw.trashWithdraw", () -> withdrawCommandService.trashedWithdraw(
                pb.withdraw.Withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to trash withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawDeleteAt> restoreWithdraw(int id) {
        return telemetryHelper.traceAndMetric("withdraw.restoreWithdraw", () -> withdrawCommandService.restoreWithdraw(
                pb.withdraw.Withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.SimpleResponse> deleteWithdrawPermanent(int id) {
        return telemetryHelper.traceAndMetric("withdraw.deleteWithdrawPermanent", () -> withdrawCommandService.deleteWithdrawPermanent(
                pb.withdraw.Withdraw.FindByIdWithdrawRequest.newBuilder()
                        .setWithdrawId(id)
                        .build())
                .map(WithdrawDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete withdraw: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.SimpleResponse> restoreAllWithdraws() {
        return telemetryHelper.traceAndMetric("withdraw.restoreAllWithdraws", () -> withdrawCommandService.restoreAllWithdraw(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(WithdrawDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore all withdraws: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.SimpleResponse> deleteAllWithdraws() {
        return telemetryHelper.traceAndMetric("withdraw.deleteAllWithdraws", () -> withdrawCommandService.deleteAllWithdrawPermanent(
                com.google.protobuf.Empty.getDefaultInstance())
                .map(WithdrawDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete all withdraws: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthAmount> findMonthlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyAmounts", () -> withdrawStatsAmountService.findMonthlyWithdraws(
                pb.withdraw.Withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearAmount> findYearlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyAmounts", () -> withdrawStatsAmountService.findYearlyWithdraws(
                pb.withdraw.Withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthAmount> findMonthlyByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyByCard", () -> withdrawStatsAmountService.findMonthlyWithdrawsByCardNumber(
                pb.withdraw.Withdraw.FindYearWithdrawCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearAmount> findYearlyByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyByCard", () -> withdrawStatsAmountService.findYearlyWithdrawsByCardNumber(
                pb.withdraw.Withdraw.FindYearWithdrawCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthStatusSuccess> findMonthlyStatusSuccess(int year, int month) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyStatusSuccess", () -> withdrawStatsStatusService.findMonthlyWithdrawStatusSuccess(
                pb.withdraw.Withdraw.FindMonthlyWithdrawStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearStatusSuccess> findYearlyStatusSuccess(int year) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyStatusSuccess", () -> withdrawStatsStatusService.findYearlyWithdrawStatusSuccess(
                pb.withdraw.Withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthStatusFailed> findMonthlyStatusFailed(int year, int month) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyStatusFailed", () -> withdrawStatsStatusService.findMonthlyWithdrawStatusFailed(
                pb.withdraw.Withdraw.FindMonthlyWithdrawStatus.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearStatusFailed> findYearlyStatusFailed(int year) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyStatusFailed", () -> withdrawStatsStatusService.findYearlyWithdrawStatusFailed(
                pb.withdraw.Withdraw.FindYearWithdrawStatus.newBuilder()
                        .setYear(year)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthStatusSuccess> findMonthlyStatusSuccessByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyStatusSuccessByCard", () -> withdrawStatsStatusService.findMonthlyWithdrawStatusSuccessCardNumber(
                pb.withdraw.Withdraw.FindMonthlyWithdrawStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearStatusSuccess> findYearlyStatusSuccessByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyStatusSuccessByCard", () -> withdrawStatsStatusService.findYearlyWithdrawStatusSuccessCardNumber(
                pb.withdraw.Withdraw.FindYearWithdrawStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawMonthStatusFailed> findMonthlyStatusFailedByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findMonthlyStatusFailedByCard", () -> withdrawStatsStatusService.findMonthlyWithdrawStatusFailedCardNumber(
                pb.withdraw.Withdraw.FindMonthlyWithdrawStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly withdraw failed by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<WithdrawDto.ApiResponseWithdrawYearStatusFailed> findYearlyStatusFailedByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("withdraw.findYearlyStatusFailedByCard", () -> withdrawStatsStatusService.findYearlyWithdrawStatusFailedCardNumber(
                pb.withdraw.Withdraw.FindYearWithdrawStatusCardNumber.newBuilder()
                        .setYear(year)
                        .setCardNumber(cardNumber == null ? "" : cardNumber)
                        .build())
                .map(WithdrawDto.ApiResponseWithdrawYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly withdraw failed by card: " + throwable.getMessage(), throwable)));
    }
}

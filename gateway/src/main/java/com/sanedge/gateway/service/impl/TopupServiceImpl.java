package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.TopupDto;
import com.sanedge.gateway.service.TopupService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TopupServiceImpl implements TopupService {

    private static final Logger LOG = Logger.getLogger(TopupServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("topup")
    pb.topup.MutinyTopupQueryServiceGrpc.MutinyTopupQueryServiceStub topupQueryService;

    @GrpcClient("topup")
    pb.topup.MutinyTopupCommandServiceGrpc.MutinyTopupCommandServiceStub topupCommandService;

    @GrpcClient("topup")
    pb.topup.stats.MutinyTopupStatsAmountServiceGrpc.MutinyTopupStatsAmountServiceStub topupStatsAmountService;

    @GrpcClient("topup")
    pb.topup.stats.MutinyTopupStatsMethodServiceGrpc.MutinyTopupStatsMethodServiceStub topupStatsMethodService;

    @GrpcClient("topup")
    pb.topup.stats.MutinyTopupStatsStatusServiceGrpc.MutinyTopupStatsStatusServiceStub topupStatsStatusService;

    @Override
    public Uni<TopupDto.ApiResponsePaginationTopup> listTopups(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("topup.listTopups", () -> topupQueryService.findAllTopup(pb.topup.TopupQuery.FindAllTopupRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(TopupDto.ApiResponsePaginationTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list topups: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponsePaginationTopup> listTopupsByCard(String cardNumber, int page, int size, String search) {
        return telemetryHelper.traceAndMetric("topup.listTopupsByCard", () -> topupQueryService.findAllTopupByCardNumber(pb.topup.TopupQuery.FindAllTopupByCardNumberRequest.newBuilder()
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(TopupDto.ApiResponsePaginationTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list topups by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponsePaginationTopupDeleteAt> findActiveTopups(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("topup.findActiveTopups", () -> topupQueryService.findByActive(pb.topup.TopupQuery.FindAllTopupRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(TopupDto.ApiResponsePaginationTopupDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find active topups: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponsePaginationTopupDeleteAt> findTrashedTopups(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("topup.findTrashedTopups", () -> topupQueryService.findByTrashed(pb.topup.TopupQuery.FindAllTopupRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(TopupDto.ApiResponsePaginationTopupDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find trashed topups: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopup> getTopup(int id) {
        return telemetryHelper.traceAndMetric("topup.getTopup", () -> topupQueryService.findByIdTopup(pb.topup.Topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id)
                .build())
                .map(TopupDto.ApiResponseTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get topup with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopup> getTopupByCard(String cardNumber, int year) {
        return telemetryHelper.traceAndMetric("topup.getTopupByCard", () -> topupQueryService.findByCardNumberTopup(pb.topup.Topup.FindByCardNumberTopupRequest.newBuilder()
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get topup by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopup> createTopup(TopupDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("topup.createTopup", () -> topupCommandService.createTopup(pb.topup.TopupCommand.CreateTopupRequest.newBuilder()
                .setCardNumber(body.cardNumber())
                .setTopupAmount(body.topupAmount())
                .setTopupMethod(body.topupMethod())
                .build())
                .map(TopupDto.ApiResponseTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create topup: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopup> updateTopup(int id, TopupDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("topup.updateTopup", () -> topupCommandService.updateTopup(pb.topup.TopupCommand.UpdateTopupRequest.newBuilder()
                .setTopupId(id)
                .setCardNumber(body.cardNumber())
                .setTopupAmount(body.topupAmount())
                .setTopupMethod(body.topupMethod())
                .build())
                .map(TopupDto.ApiResponseTopup::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update topup: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.SimpleResponse> deleteTopupPermanent(int id) {
        return telemetryHelper.traceAndMetric("topup.deleteTopupPermanent", () -> topupCommandService.deleteTopupPermanent(pb.topup.Topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id)
                .build())
                .map(TopupDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to permanently delete topup: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupDeleteAt> trashTopup(int id) {
        return telemetryHelper.traceAndMetric("topup.trashTopup", () -> topupCommandService.trashedTopup(pb.topup.Topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id)
                .build())
                .map(TopupDto.ApiResponseTopupDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to trash topup: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupDeleteAt> restoreTopup(int id) {
        return telemetryHelper.traceAndMetric("topup.restoreTopup", () -> topupCommandService.restoreTopup(pb.topup.Topup.FindByIdTopupRequest.newBuilder()
                .setTopupId(id)
                .build())
                .map(TopupDto.ApiResponseTopupDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore topup: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.SimpleResponse> restoreAllTopups() {
        return telemetryHelper.traceAndMetric("topup.restoreAllTopups", () -> topupCommandService.restoreAllTopup(com.google.protobuf.Empty.getDefaultInstance())
                .map(TopupDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to restore all topups: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.SimpleResponse> deleteAllTopups() {
        return telemetryHelper.traceAndMetric("topup.deleteAllTopups", () -> topupCommandService.deleteAllTopupPermanent(com.google.protobuf.Empty.getDefaultInstance())
                .map(TopupDto.SimpleResponse::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to delete all topups: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthAmount> getMonthlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyAmounts", () -> topupStatsAmountService.findMonthlyTopupAmounts(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearAmount> getYearlyAmounts(int year) {
        return telemetryHelper.traceAndMetric("topup.getYearlyAmounts", () -> topupStatsAmountService.findYearlyTopupAmounts(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup amounts: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthAmount> getMonthlyAmountsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyAmountsByCard", () -> topupStatsAmountService.findMonthlyTopupAmountsByCardNumber(pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupMonthAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearAmount> getYearlyAmountsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getYearlyAmountsByCard", () -> topupStatsAmountService.findYearlyTopupAmountsByCardNumber(pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupYearAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup amounts by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthMethod> getMonthlyMethods(int year) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyMethods", () -> topupStatsMethodService.findMonthlyTopupMethods(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupMonthMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup methods: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearMethod> getYearlyMethods(int year) {
        return telemetryHelper.traceAndMetric("topup.getYearlyMethods", () -> topupStatsMethodService.findYearlyTopupMethods(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupYearMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup methods: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthMethod> getMonthlyMethodsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyMethodsByCard", () -> topupStatsMethodService.findMonthlyTopupMethodsByCardNumber(pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupMonthMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup methods by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearMethod> getYearlyMethodsByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getYearlyMethodsByCard", () -> topupStatsMethodService.findYearlyTopupMethodsByCardNumber(pb.topup.Topup.FindYearTopupCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupYearMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup methods by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthStatusSuccess> getMonthlyStatusSuccess(int year, int month) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyStatusSuccess", () -> topupStatsStatusService.findMonthlyTopupStatusSuccess(pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build())
                .map(TopupDto.ApiResponseTopupMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearStatusSuccess> getYearlyStatusSuccess(int year) {
        return telemetryHelper.traceAndMetric("topup.getYearlyStatusSuccess", () -> topupStatsStatusService.findYearlyTopupStatusSuccess(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup success stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthStatusFailed> getMonthlyStatusFailed(int year, int month) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyStatusFailed", () -> topupStatsStatusService.findMonthlyTopupStatusFailed(pb.topup.Topup.FindMonthlyTopupStatus.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build())
                .map(TopupDto.ApiResponseTopupMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearStatusFailed> getYearlyStatusFailed(int year) {
        return telemetryHelper.traceAndMetric("topup.getYearlyStatusFailed", () -> topupStatsStatusService.findYearlyTopupStatusFailed(pb.topup.Topup.FindYearTopupStatus.newBuilder()
                .setYear(year)
                .build())
                .map(TopupDto.ApiResponseTopupYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup failed stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthStatusSuccess> getMonthlyStatusSuccessByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyStatusSuccessByCard", () -> topupStatsStatusService.findMonthlyTopupStatusSuccessByCardNumber(pb.topup.Topup.FindMonthlyTopupStatusCardNumber.newBuilder()
                .setYear(year)
                .setMonth(month)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupMonthStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearStatusSuccess> getYearlyStatusSuccessByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getYearlyStatusSuccessByCard", () -> topupStatsStatusService.findYearlyTopupStatusSuccessByCardNumber(pb.topup.Topup.FindYearTopupStatusCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupYearStatusSuccess::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup success by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupMonthStatusFailed> getMonthlyStatusFailedByCard(int year, int month, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getMonthlyStatusFailedByCard", () -> topupStatsStatusService.findMonthlyTopupStatusFailedByCardNumber(pb.topup.Topup.FindMonthlyTopupStatusCardNumber.newBuilder()
                .setYear(year)
                .setMonth(month)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupMonthStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly topup failed by card: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<TopupDto.ApiResponseTopupYearStatusFailed> getYearlyStatusFailedByCard(int year, String cardNumber) {
        return telemetryHelper.traceAndMetric("topup.getYearlyStatusFailedByCard", () -> topupStatsStatusService.findYearlyTopupStatusFailedByCardNumber(pb.topup.Topup.FindYearTopupStatusCardNumber.newBuilder()
                .setYear(year)
                .setCardNumber(cardNumber == null ? "" : cardNumber)
                .build())
                .map(TopupDto.ApiResponseTopupYearStatusFailed::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly topup failed by card: " + throwable.getMessage(), throwable)));
    }
}

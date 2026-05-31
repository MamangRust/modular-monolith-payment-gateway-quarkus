package com.sanedge.gateway.service.impl;

import com.sanedge.gateway.dto.MerchantDto;
import com.sanedge.gateway.service.MerchantService;
import com.sanedge.gateway.telemetry.TelemetryHelper;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MerchantServiceImpl implements MerchantService {

    private static final Logger LOG = Logger.getLogger(MerchantServiceImpl.class);

    @Inject
    TelemetryHelper telemetryHelper;

    @GrpcClient("merchant")
    pb.merchant.MutinyMerchantQueryServiceGrpc.MutinyMerchantQueryServiceStub merchantQueryService;

    @GrpcClient("merchant")
    pb.merchant.MutinyMerchantCommandServiceGrpc.MutinyMerchantCommandServiceStub merchantCommandService;

    @GrpcClient("merchant")
    pb.merchant.MutinyMerchantTransactionServiceGrpc.MutinyMerchantTransactionServiceStub merchantTransactionService;

    @GrpcClient("merchant")
    pb.merchant.stats.MutinyMerchantStatsAmountServiceGrpc.MutinyMerchantStatsAmountServiceStub merchantStatsAmountService;

    @GrpcClient("merchant")
    pb.merchant.stats.MutinyMerchantStatsMethodServiceGrpc.MutinyMerchantStatsMethodServiceStub merchantStatsMethodService;

    @GrpcClient("merchant")
    pb.merchant.stats.MutinyMerchantStatsTotalAmountServiceGrpc.MutinyMerchantStatsTotalAmountServiceStub merchantStatsTotalAmountService;

    @Override
    public Uni<MerchantDto.ApiResponsePaginationMerchant> listMerchants(int page, int size, String search) {
        return telemetryHelper.traceAndMetric("merchant.listMerchants", () -> merchantQueryService.findAllMerchant(pb.merchant.Merchant.FindAllMerchantRequest.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .build())
                .map(MerchantDto.ApiResponsePaginationMerchant::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to list merchants: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchant> getMerchant(int id) {
        return telemetryHelper.traceAndMetric("merchant.getMerchant", () -> merchantQueryService.findByIdMerchant(pb.merchant.Merchant.FindByIdMerchantRequest.newBuilder()
                .setMerchantId(id)
                .build())
                .map(MerchantDto.ApiResponseMerchant::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get merchant with id " + id + ": " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchant> createMerchant(MerchantDto.CreateRequest body) {
        return telemetryHelper.traceAndMetric("merchant.createMerchant", () -> merchantCommandService.createMerchant(pb.merchant.MerchantCommand.CreateMerchantRequest.newBuilder()
                .setName(body.name())
                .setUserId(body.userId())
                .build())
                .map(MerchantDto.ApiResponseMerchant::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to create merchant: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchant> updateMerchant(int id, MerchantDto.UpdateRequest body) {
        return telemetryHelper.traceAndMetric("merchant.updateMerchant", () -> merchantCommandService.updateMerchant(pb.merchant.MerchantCommand.UpdateMerchantRequest.newBuilder()
                .setMerchantId(id)
                .setName(body.name())
                .setUserId(body.userId())
                .setStatus(body.status() == null ? "" : body.status())
                .build())
                .map(MerchantDto.ApiResponseMerchant::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to update merchant: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantDeleteAt> deleteMerchant(int id) {
        return telemetryHelper.traceAndMetric("merchant.deleteMerchant", () -> merchantCommandService.trashedMerchant(pb.merchant.Merchant.FindByIdMerchantRequest.newBuilder()
                .setMerchantId(id)
                .build())
                .map(MerchantDto.ApiResponseMerchantDeleteAt::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to soft-delete merchant: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findAllTransactions(int page, int size, String search, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.findAllTransactions", () -> merchantTransactionService.findAllTransactionMerchant(pb.merchant.Merchant.FindAllMerchantTransaction.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponsePaginationMerchantTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find all transactions: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findTransactionsById(int page, int size, String search, String id) {
        return telemetryHelper.traceAndMetric("merchant.findTransactionsById", () -> merchantTransactionService.findAllTransactionByMerchant(pb.merchant.Merchant.FindAllMerchantTransactionId.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .setId(id == null ? "" : id)
                .build())
                .map(MerchantDto.ApiResponsePaginationMerchantTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find transactions by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponsePaginationMerchantTransaction> findTransactionsByApiKey(int page, int size, String search, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.findTransactionsByApiKey", () -> merchantTransactionService.findAllTransactionByApikey(pb.merchant.Merchant.FindAllMerchantTransactionApikey.newBuilder()
                .setPage(page)
                .setPageSize(size)
                .setSearch(search == null ? "" : search)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponsePaginationMerchantTransaction::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to find transactions by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmount(int year) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyAmount", () -> merchantStatsAmountService.findMonthlyAmountMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly amount stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmount(int year) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyAmount", () -> merchantStatsAmountService.findYearlyAmountMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly amount stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmountById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyAmountById", () -> merchantStatsAmountService.findMonthlyAmountByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly amount by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmountById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyAmountById", () -> merchantStatsAmountService.findYearlyAmountByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly amount by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyAmount> getMonthlyAmountByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyAmountByApiKey", () -> merchantStatsAmountService.findMonthlyAmountByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly amount by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyAmount> getYearlyAmountByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyAmountByApiKey", () -> merchantStatsAmountService.findYearlyAmountByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly amount by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethod(int year) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyMethod", () -> merchantStatsMethodService.findMonthlyPaymentMethodsMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly payment method: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethod(int year) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyMethod", () -> merchantStatsMethodService.findYearlyPaymentMethodMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly payment method: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethodById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyMethodById", () -> merchantStatsMethodService.findMonthlyPaymentMethodByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly payment method by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethodById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyMethodById", () -> merchantStatsMethodService.findYearlyPaymentMethodByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly payment method by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyPaymentMethod> getMonthlyMethodByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyMethodByApiKey", () -> merchantStatsMethodService.findMonthlyPaymentMethodByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly payment method by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyPaymentMethod> getYearlyMethodByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyMethodByApiKey", () -> merchantStatsMethodService.findYearlyPaymentMethodByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyPaymentMethod::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly payment method by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmount(int year) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyTotalAmount", () -> merchantStatsTotalAmountService.findMonthlyTotalAmountMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly total amount stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmount(int year) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyTotalAmount", () -> merchantStatsTotalAmountService.findYearlyTotalAmountMerchant(pb.merchant.Merchant.FindYearMerchant.newBuilder()
                .setYear(year)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly total amount stats: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmountById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyTotalAmountById", () -> merchantStatsTotalAmountService.findMonthlyTotalAmountByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly total amount by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmountById(int year, int merchantId) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyTotalAmountById", () -> merchantStatsTotalAmountService.findYearlyTotalAmountByMerchants(pb.merchant.Merchant.FindYearMerchantById.newBuilder()
                .setYear(year)
                .setMerchantId(merchantId)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly total amount by id: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantMonthlyTotalAmount> getMonthlyTotalAmountByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getMonthlyTotalAmountByApiKey", () -> merchantStatsTotalAmountService.findMonthlyTotalAmountByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantMonthlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get monthly total amount by apiKey: " + throwable.getMessage(), throwable)));
    }

    @Override
    public Uni<MerchantDto.ApiResponseMerchantYearlyTotalAmount> getYearlyTotalAmountByApiKey(int year, String apiKey) {
        return telemetryHelper.traceAndMetric("merchant.getYearlyTotalAmountByApiKey", () -> merchantStatsTotalAmountService.findYearlyTotalAmountByApikey(pb.merchant.Merchant.FindYearMerchantByApikey.newBuilder()
                .setYear(year)
                .setApiKey(apiKey == null ? "" : apiKey)
                .build())
                .map(MerchantDto.ApiResponseMerchantYearlyTotalAmount::from)
                .onFailure().invoke(throwable -> LOG.error("Failed to get yearly total amount by apiKey: " + throwable.getMessage(), throwable)));
    }
}

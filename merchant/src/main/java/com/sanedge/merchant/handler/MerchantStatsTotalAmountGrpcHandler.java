package com.sanedge.merchant.handler;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearTotalAmountApiKey;
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearTotalAmountMerchant;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountByApiKeyService;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountByIdService;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.merchant.Merchant.FindYearMerchant;
import pb.merchant.Merchant.FindYearMerchantByApikey;
import pb.merchant.Merchant.FindYearMerchantById;
import pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount;
import pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantYearlyTotalAmount;
import pb.merchant.stats.MutinyMerchantStatsTotalAmountServiceGrpc;

@GrpcService
@Singleton
public class MerchantStatsTotalAmountGrpcHandler
        extends MutinyMerchantStatsTotalAmountServiceGrpc.MerchantStatsTotalAmountServiceImplBase {

    @Inject
    MerchantTotalAmountService merchantTotalAmountService;

    @Inject
    MerchantTotalAmountByIdService merchantTotalAmountByIdService;

    @Inject
    MerchantTotalAmountByApiKeyService merchantTotalAmountByApiKeyService;

    @Override
    public Uni<ApiResponseMerchantMonthlyTotalAmount> findMonthlyTotalAmountMerchant(FindYearMerchant request) {
        return merchantTotalAmountService.findMonthTotalAmount((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyTotalAmount.Builder builder = ApiResponseMerchantMonthlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount mta : apiResp
                                .data()) {
                            builder.addData(toProto(mta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyTotalAmount> findYearlyTotalAmountMerchant(FindYearMerchant request) {
        return merchantTotalAmountService.findYearTotalAmount((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantYearlyTotalAmount.Builder builder = ApiResponseMerchantYearlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount yta : apiResp
                                .data()) {
                            builder.addData(toProto(yta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyTotalAmount> findMonthlyTotalAmountByMerchants(FindYearMerchantById request) {
        MonthYearTotalAmountMerchant domainReq = new MonthYearTotalAmountMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantTotalAmountByIdService.findMonthTotalAmountById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyTotalAmount.Builder builder = ApiResponseMerchantMonthlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount mta : apiResp
                                .data()) {
                            builder.addData(toProto(mta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyTotalAmount> findYearlyTotalAmountByMerchants(FindYearMerchantById request) {
        MonthYearTotalAmountMerchant domainReq = new MonthYearTotalAmountMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantTotalAmountByIdService.findYearTotalAmountById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyTotalAmount.Builder builder = ApiResponseMerchantYearlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount yta : apiResp
                                .data()) {
                            builder.addData(toProto(yta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyTotalAmount> findMonthlyTotalAmountByApikey(FindYearMerchantByApikey request) {
        MonthYearTotalAmountApiKey domainReq = new MonthYearTotalAmountApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantTotalAmountByApiKeyService.findMonthTotalAmountByApiKey(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyTotalAmount.Builder builder = ApiResponseMerchantMonthlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount mta : apiResp
                                .data()) {
                            builder.addData(toProto(mta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyTotalAmount> findYearlyTotalAmountByApikey(FindYearMerchantByApikey request) {
        MonthYearTotalAmountApiKey domainReq = new MonthYearTotalAmountApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantTotalAmountByApiKeyService.findYearTotalAmountByApiKey(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyTotalAmount.Builder builder = ApiResponseMerchantYearlyTotalAmount
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount yta : apiResp
                                .data()) {
                            builder.addData(toProto(yta));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseMonthlyTotalAmount toProto(
            com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount m) {
        if (m == null) {
            return pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseMonthlyTotalAmount.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseMonthlyTotalAmount.Builder b = pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseMonthlyTotalAmount
                .newBuilder();
        if (m.getMonthName() != null)
            b.setMonth(m.getMonthName());
        if (m.getReportYear() != null)
            b.setYear(m.getReportYear());
        if (m.getTotalAmount() != null)
            b.setTotalAmount(m.getTotalAmount());
        return b.build();
    }

    private pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseYearlyTotalAmount toProto(
            com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount y) {
        if (y == null) {
            return pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseYearlyTotalAmount.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseYearlyTotalAmount.Builder b = pb.merchant.stats.MerchantStatsTotalamount.MerchantResponseYearlyTotalAmount
                .newBuilder();
        if (y.getReportYear() != null)
            b.setYear(y.getReportYear());
        if (y.getTotalAmount() != null)
            b.setTotalAmount(y.getTotalAmount());
        return b.build();
    }
}

package com.sanedge.merchant.handler;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearAmountApiKey;
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearAmountMerchant;
import com.sanedge.merchant.service.stats.amount.MerchantAmountByApiKeyService;
import com.sanedge.merchant.service.stats.amount.MerchantAmountByIdService;
import com.sanedge.merchant.service.stats.amount.MerchantAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.merchant.Merchant.FindYearMerchant;
import pb.merchant.Merchant.FindYearMerchantByApikey;
import pb.merchant.Merchant.FindYearMerchantById;
import pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantMonthlyAmount;
import pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantYearlyAmount;
import pb.merchant.stats.MutinyMerchantStatsAmountServiceGrpc;

@GrpcService
@Singleton
public class MerchantStatsAmountGrpcHandler
        extends MutinyMerchantStatsAmountServiceGrpc.MerchantStatsAmountServiceImplBase {

    @Inject
    MerchantAmountService merchantAmountService;

    @Inject
    MerchantAmountByIdService merchantAmountByIdService;

    @Inject
    MerchantAmountByApiKeyService merchantAmountByApiKeyService;

    @Override
    public Uni<ApiResponseMerchantMonthlyAmount> findMonthlyAmountMerchant(FindYearMerchant request) {
        return merchantAmountService.findMonthAmount((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyAmount.Builder builder = ApiResponseMerchantMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount ma : apiResp
                                .data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyAmount> findYearlyAmountMerchant(FindYearMerchant request) {
        return merchantAmountService.findYearAmount((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantYearlyAmount.Builder builder = ApiResponseMerchantYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount ya : apiResp
                                .data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyAmount> findMonthlyAmountByMerchants(FindYearMerchantById request) {
        MonthYearAmountMerchant domainReq = new MonthYearAmountMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantAmountByIdService.findMonthAmountById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyAmount.Builder builder = ApiResponseMerchantMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount ma : apiResp
                                .data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyAmount> findYearlyAmountByMerchants(FindYearMerchantById request) {
        MonthYearAmountMerchant domainReq = new MonthYearAmountMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantAmountByIdService.findYearAmountById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyAmount.Builder builder = ApiResponseMerchantYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount ya : apiResp
                                .data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyAmount> findMonthlyAmountByApikey(FindYearMerchantByApikey request) {
        MonthYearAmountApiKey domainReq = new MonthYearAmountApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantAmountByApiKeyService.findMonthAmountByApiKey(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyAmount.Builder builder = ApiResponseMerchantMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount ma : apiResp
                                .data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyAmount> findYearlyAmountByApikey(FindYearMerchantByApikey request) {
        MonthYearAmountApiKey domainReq = new MonthYearAmountApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantAmountByApiKeyService.findYearAmountByApiKey(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyAmount.Builder builder = ApiResponseMerchantYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount ya : apiResp
                                .data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private pb.merchant.stats.MerchantStatsAmount.MerchantResponseMonthlyAmount toProto(
            com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount m) {
        if (m == null) {
            return pb.merchant.stats.MerchantStatsAmount.MerchantResponseMonthlyAmount.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsAmount.MerchantResponseMonthlyAmount.Builder b = pb.merchant.stats.MerchantStatsAmount.MerchantResponseMonthlyAmount
                .newBuilder();
        if (m.getMonthName() != null)
            b.setMonth(m.getMonthName());
        if (m.getTotalAmount() != null)
            b.setTotalAmount(m.getTotalAmount());
        return b.build();
    }

    private pb.merchant.stats.MerchantStatsAmount.MerchantResponseYearlyAmount toProto(
            com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount y) {
        if (y == null) {
            return pb.merchant.stats.MerchantStatsAmount.MerchantResponseYearlyAmount.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsAmount.MerchantResponseYearlyAmount.Builder b = pb.merchant.stats.MerchantStatsAmount.MerchantResponseYearlyAmount
                .newBuilder();
        if (y.getReportYear() != null)
            b.setYear(y.getReportYear());
        if (y.getTotalAmount() != null)
            b.setTotalAmount(y.getTotalAmount());
        return b.build();
    }
}

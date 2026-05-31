package com.sanedge.merchant.handler;

import com.sanedge.merchant.domain.requests.statsbyapikey.MonthYearPaymentMethodApiKey;
import com.sanedge.merchant.domain.requests.statsbyid.MonthYearPaymentMethodMerchant;
import com.sanedge.merchant.service.stats.method.MerchantMethodByApiKeyService;
import com.sanedge.merchant.service.stats.method.MerchantMethodByIdService;
import com.sanedge.merchant.service.stats.method.MerchantMethodService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.merchant.Merchant.FindYearMerchant;
import pb.merchant.Merchant.FindYearMerchantByApikey;
import pb.merchant.Merchant.FindYearMerchantById;
import pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod;
import pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantYearlyPaymentMethod;
import pb.merchant.stats.MutinyMerchantStatsMethodServiceGrpc;

@GrpcService
@Singleton
public class MerchantStatsMethodGrpcHandler
        extends MutinyMerchantStatsMethodServiceGrpc.MerchantStatsMethodServiceImplBase {

    @Inject
    MerchantMethodService merchantMethodService;

    @Inject
    MerchantMethodByIdService merchantMethodByIdService;

    @Inject
    MerchantMethodByApiKeyService merchantMethodByApiKeyService;

    @Override
    public Uni<ApiResponseMerchantMonthlyPaymentMethod> findMonthlyPaymentMethodsMerchant(FindYearMerchant request) {
        return merchantMethodService.findMonthMethod((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyPaymentMethod.Builder builder = ApiResponseMerchantMonthlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod mm : apiResp
                                .data()) {
                            builder.addData(toProto(mm));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyPaymentMethod> findYearlyPaymentMethodMerchant(FindYearMerchant request) {
        return merchantMethodService.findYearMethod((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMerchantYearlyPaymentMethod.Builder builder = ApiResponseMerchantYearlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod ym : apiResp
                                .data()) {
                            builder.addData(toProto(ym));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyPaymentMethod> findMonthlyPaymentMethodByMerchants(
            FindYearMerchantById request) {
        MonthYearPaymentMethodMerchant domainReq = new MonthYearPaymentMethodMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantMethodByIdService.findMonthMethodById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyPaymentMethod.Builder builder = ApiResponseMerchantMonthlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod mm : apiResp
                                .data()) {
                            builder.addData(toProto(mm));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyPaymentMethod> findYearlyPaymentMethodByMerchants(
            FindYearMerchantById request) {
        MonthYearPaymentMethodMerchant domainReq = new MonthYearPaymentMethodMerchant();
        domainReq.setMerchantId((long) request.getMerchantId());
        domainReq.setYear((long) request.getYear());

        return merchantMethodByIdService.findYearMethodById(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyPaymentMethod.Builder builder = ApiResponseMerchantYearlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod ym : apiResp
                                .data()) {
                            builder.addData(toProto(ym));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantMonthlyPaymentMethod> findMonthlyPaymentMethodByApikey(
            FindYearMerchantByApikey request) {
        MonthYearPaymentMethodApiKey domainReq = new MonthYearPaymentMethodApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantMethodByApiKeyService.findMonthMethod(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantMonthlyPaymentMethod.Builder builder = ApiResponseMerchantMonthlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod mm : apiResp
                                .data()) {
                            builder.addData(toProto(mm));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMerchantYearlyPaymentMethod> findYearlyPaymentMethodByApikey(
            FindYearMerchantByApikey request) {
        MonthYearPaymentMethodApiKey domainReq = new MonthYearPaymentMethodApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setYear((long) request.getYear());

        return merchantMethodByApiKeyService.findYearMethod(domainReq)
                .map(apiResp -> {
                    ApiResponseMerchantYearlyPaymentMethod.Builder builder = ApiResponseMerchantYearlyPaymentMethod
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod ym : apiResp
                                .data()) {
                            builder.addData(toProto(ym));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private pb.merchant.stats.MerchantStatsMethod.MerchantResponseMonthlyPaymentMethod toProto(
            com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod m) {
        if (m == null) {
            return pb.merchant.stats.MerchantStatsMethod.MerchantResponseMonthlyPaymentMethod.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsMethod.MerchantResponseMonthlyPaymentMethod.Builder b = pb.merchant.stats.MerchantStatsMethod.MerchantResponseMonthlyPaymentMethod
                .newBuilder();
        if (m.getMonthName() != null)
            b.setMonth(m.getMonthName());
        if (m.getPaymentMethod() != null)
            b.setPaymentMethod(m.getPaymentMethod());
        if (m.getTotalAmount() != null)
            b.setTotalAmount(m.getTotalAmount());
        return b.build();
    }

    private pb.merchant.stats.MerchantStatsMethod.MerchantResponseYearlyPaymentMethod toProto(
            com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod y) {
        if (y == null) {
            return pb.merchant.stats.MerchantStatsMethod.MerchantResponseYearlyPaymentMethod.getDefaultInstance();
        }
        pb.merchant.stats.MerchantStatsMethod.MerchantResponseYearlyPaymentMethod.Builder b = pb.merchant.stats.MerchantStatsMethod.MerchantResponseYearlyPaymentMethod
                .newBuilder();
        if (y.getReportYear() != null)
            b.setYear(y.getReportYear());
        if (y.getPaymentMethod() != null)
            b.setPaymentMethod(y.getPaymentMethod());
        if (y.getTotalAmount() != null)
            b.setTotalAmount(y.getTotalAmount());
        return b.build();
    }
}

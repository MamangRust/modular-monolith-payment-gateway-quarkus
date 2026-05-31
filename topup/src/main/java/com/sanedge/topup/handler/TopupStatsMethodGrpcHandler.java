package com.sanedge.topup.handler;

import com.sanedge.topup.domain.requests.stats.YearMonthMethod;
import com.sanedge.topup.domain.response.stats.method.TopupMonthMethodResponse;
import com.sanedge.topup.domain.response.stats.method.TopupYearlyMethodResponse;
import com.sanedge.topup.service.stats.method.TopupMethodByCardService;
import com.sanedge.topup.service.stats.method.TopupMethodService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.topup.stats.MutinyTopupStatsMethodServiceGrpc;
import pb.topup.stats.TopupStatsMethod.ApiResponseTopupMonthMethod;
import pb.topup.stats.TopupStatsMethod.ApiResponseTopupYearMethod;

@GrpcService
@Singleton
public class TopupStatsMethodGrpcHandler extends MutinyTopupStatsMethodServiceGrpc.TopupStatsMethodServiceImplBase {

    @Inject
    TopupMethodService topupMethodService;

    @Inject
    TopupMethodByCardService topupMethodByCardService;

    @Override
    public Uni<ApiResponseTopupMonthMethod> findMonthlyTopupMethods(pb.topup.Topup.FindYearTopupStatus request) {
        return topupMethodService.findMonthlyMethods((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupMonthMethod.Builder builder = ApiResponseTopupMonthMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupMonthMethodResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsMethod.TopupMonthMethodResponse.newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setTopupMethod(r.getTopupMethod())
                                    .setTotalTopups(r.getTotalTopups().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupYearMethod> findYearlyTopupMethods(pb.topup.Topup.FindYearTopupStatus request) {
        return topupMethodService.findYearlyMethods((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupYearMethod.Builder builder = ApiResponseTopupYearMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupYearlyMethodResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsMethod.TopupYearlyMethodResponse.newBuilder()
                                    .setYear(r.getReportYear())
                                    .setTopupMethod(r.getTopupMethod())
                                    .setTotalTopups(r.getTotalTopups().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupMonthMethod> findMonthlyTopupMethodsByCardNumber(
            pb.topup.Topup.FindYearTopupCardNumber request) {
        YearMonthMethod req = new YearMonthMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupMethodByCardService.findMonthlyMethods(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthMethod.Builder builder = ApiResponseTopupMonthMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupMonthMethodResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsMethod.TopupMonthMethodResponse.newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setTopupMethod(r.getTopupMethod())
                                    .setTotalTopups(r.getTotalTopups().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupYearMethod> findYearlyTopupMethodsByCardNumber(
            pb.topup.Topup.FindYearTopupCardNumber request) {
        YearMonthMethod req = new YearMonthMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupMethodByCardService.findYearlyMethods(req)
                .map(apiResp -> {
                    ApiResponseTopupYearMethod.Builder builder = ApiResponseTopupYearMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupYearlyMethodResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsMethod.TopupYearlyMethodResponse.newBuilder()
                                    .setYear(r.getReportYear())
                                    .setTopupMethod(r.getTopupMethod())
                                    .setTotalTopups(r.getTotalTopups().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

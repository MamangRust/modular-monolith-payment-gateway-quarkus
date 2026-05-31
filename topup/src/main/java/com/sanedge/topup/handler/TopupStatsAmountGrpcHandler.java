package com.sanedge.topup.handler;

import com.sanedge.topup.domain.requests.stats.YearMonthMethod;
import com.sanedge.topup.domain.response.stats.amount.TopupMonthAmountResponse;
import com.sanedge.topup.domain.response.stats.amount.TopupYearlyAmountResponse;
import com.sanedge.topup.service.stats.amount.TopupAmountByCardService;
import com.sanedge.topup.service.stats.amount.TopupAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.topup.stats.MutinyTopupStatsAmountServiceGrpc;
import pb.topup.stats.TopupStatsAmount.ApiResponseTopupMonthAmount;
import pb.topup.stats.TopupStatsAmount.ApiResponseTopupYearAmount;

@GrpcService
@Singleton
public class TopupStatsAmountGrpcHandler extends MutinyTopupStatsAmountServiceGrpc.TopupStatsAmountServiceImplBase {

    @Inject
    TopupAmountService topupAmountService;

    @Inject
    TopupAmountByCardService topupAmountByCardService;

    @Override
    public Uni<ApiResponseTopupMonthAmount> findMonthlyTopupAmounts(pb.topup.Topup.FindYearTopupStatus request) {
        return topupAmountService.findMonthlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupMonthAmount.Builder builder = ApiResponseTopupMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupMonthAmountResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsAmount.TopupMonthAmountResponse.newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupYearAmount> findYearlyTopupAmounts(pb.topup.Topup.FindYearTopupStatus request) {
        return topupAmountService.findYearlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupYearAmount.Builder builder = ApiResponseTopupYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsAmount.TopupYearlyAmountResponse.newBuilder()
                                    .setYear(r.getReportYear())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupMonthAmount> findMonthlyTopupAmountsByCardNumber(
            pb.topup.Topup.FindYearTopupCardNumber request) {
        YearMonthMethod req = new YearMonthMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupAmountByCardService.findMonthlyAmounts(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthAmount.Builder builder = ApiResponseTopupMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupMonthAmountResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsAmount.TopupMonthAmountResponse.newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTopupYearAmount> findYearlyTopupAmountsByCardNumber(
            pb.topup.Topup.FindYearTopupCardNumber request) {
        YearMonthMethod req = new YearMonthMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupAmountByCardService.findYearlyAmounts(req)
                .map(apiResp -> {
                    ApiResponseTopupYearAmount.Builder builder = ApiResponseTopupYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsAmount.TopupYearlyAmountResponse.newBuilder()
                                    .setYear(r.getReportYear())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

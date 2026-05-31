package com.sanedge.withdraw.handler;

import com.sanedge.withdraw.domain.requests.statsbycard.YearMonthCardNumber;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawMonthlyAmountResponse;
import com.sanedge.withdraw.domain.response.stats.amount.WithdrawYearlyAmountResponse;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountByCardService;
import com.sanedge.withdraw.service.stats.amount.WithdrawAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.withdraw.stats.MutinyWithdrawStatsAmountServiceGrpc;
import pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawMonthAmount;
import pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawYearAmount;

@GrpcService
@Singleton
public class WithdrawStatsAmountGrpcHandler
        extends MutinyWithdrawStatsAmountServiceGrpc.WithdrawStatsAmountServiceImplBase {

    @Inject
    WithdrawAmountService withdrawAmountService;

    @Inject
    WithdrawAmountByCardService withdrawAmountByCardService;

    @Override
    public Uni<ApiResponseWithdrawMonthAmount> findMonthlyWithdraws(
            pb.withdraw.Withdraw.FindYearWithdrawStatus request) {
        return withdrawAmountService.findMonthlyWithdraws((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseWithdrawMonthAmount.Builder builder = ApiResponseWithdrawMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawMonthlyAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsAmount.WithdrawMonthlyAmountResponse.newBuilder()
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
    public Uni<ApiResponseWithdrawYearAmount> findYearlyWithdraws(pb.withdraw.Withdraw.FindYearWithdrawStatus request) {
        return withdrawAmountService.findYearlyWithdraws((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseWithdrawYearAmount.Builder builder = ApiResponseWithdrawYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsAmount.WithdrawYearlyAmountResponse.newBuilder()
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
    public Uni<ApiResponseWithdrawMonthAmount> findMonthlyWithdrawsByCardNumber(
            pb.withdraw.Withdraw.FindYearWithdrawCardNumber request) {
        YearMonthCardNumber req = new YearMonthCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return withdrawAmountByCardService.findMonthlyByCardNumber(req)
                .map(apiResp -> {
                    ApiResponseWithdrawMonthAmount.Builder builder = ApiResponseWithdrawMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawMonthlyAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsAmount.WithdrawMonthlyAmountResponse.newBuilder()
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
    public Uni<ApiResponseWithdrawYearAmount> findYearlyWithdrawsByCardNumber(
            pb.withdraw.Withdraw.FindYearWithdrawCardNumber request) {
        YearMonthCardNumber req = new YearMonthCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return withdrawAmountByCardService.findYearlyByCardNumber(req)
                .map(apiResp -> {
                    ApiResponseWithdrawYearAmount.Builder builder = ApiResponseWithdrawYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsAmount.WithdrawYearlyAmountResponse.newBuilder()
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

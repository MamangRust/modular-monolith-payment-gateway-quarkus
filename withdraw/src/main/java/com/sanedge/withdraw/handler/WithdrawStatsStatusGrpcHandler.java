package com.sanedge.withdraw.handler;

import com.sanedge.withdraw.domain.requests.MonthStatusWithdraw;
import com.sanedge.withdraw.domain.requests.statsbycard.MonthStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.requests.statsbycard.YearStatusWithdrawCardNumber;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseMonthStatusSuccess;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusFailed;
import com.sanedge.withdraw.domain.response.stats.status.WithdrawResponseYearStatusSuccess;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusByCardService;
import com.sanedge.withdraw.service.stats.status.WithdrawStatusService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.withdraw.stats.MutinyWithdrawStatsStatusServiceGrpc;
import pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusFailed;
import pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess;
import pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawYearStatusFailed;
import pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawYearStatusSuccess;

@GrpcService
@Singleton
public class WithdrawStatsStatusGrpcHandler
        extends MutinyWithdrawStatsStatusServiceGrpc.WithdrawStatsStatusServiceImplBase {

    @Inject
    WithdrawStatusService withdrawStatusService;

    @Inject
    WithdrawStatusByCardService withdrawStatusByCardService;

    @Override
    public Uni<ApiResponseWithdrawMonthStatusSuccess> findMonthlyWithdrawStatusSuccess(
            pb.withdraw.Withdraw.FindMonthlyWithdrawStatus request) {
        MonthStatusWithdraw req = new MonthStatusWithdraw();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return withdrawStatusService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseWithdrawMonthStatusSuccess.Builder builder = ApiResponseWithdrawMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusSuccessResponse
                                    .newBuilder()
                                    .setYear(r.getReportYear())
                                    .setMonth(r.getMonthName())
                                    .setTotalSuccess(r.getTotalSuccess().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawYearStatusSuccess> findYearlyWithdrawStatusSuccess(
            pb.withdraw.Withdraw.FindYearWithdrawStatus request) {
        return withdrawStatusService.findYearlyStatusSuccess((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseWithdrawYearStatusSuccess.Builder builder = ApiResponseWithdrawYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusSuccessResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setTotalSuccess(r.getTotalSuccess().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawMonthStatusFailed> findMonthlyWithdrawStatusFailed(
            pb.withdraw.Withdraw.FindMonthlyWithdrawStatus request) {
        MonthStatusWithdraw req = new MonthStatusWithdraw();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return withdrawStatusService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseWithdrawMonthStatusFailed.Builder builder = ApiResponseWithdrawMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusFailedResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setMonth(r.getMonthName())
                                            .setTotalFailed(r.getTotalFailed().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawYearStatusFailed> findYearlyWithdrawStatusFailed(
            pb.withdraw.Withdraw.FindYearWithdrawStatus request) {
        return withdrawStatusService.findYearlyStatusFailed((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseWithdrawYearStatusFailed.Builder builder = ApiResponseWithdrawYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusFailedResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setTotalFailed(r.getTotalFailed().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawMonthStatusSuccess> findMonthlyWithdrawStatusSuccessCardNumber(
            pb.withdraw.Withdraw.FindMonthlyWithdrawStatusCardNumber request) {
        MonthStatusWithdrawCardNumber req = new MonthStatusWithdrawCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return withdrawStatusByCardService.findMonthStatusSuccessByCard(req)
                .map(apiResp -> {
                    ApiResponseWithdrawMonthStatusSuccess.Builder builder = ApiResponseWithdrawMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusSuccessResponse
                                    .newBuilder()
                                    .setYear(r.getReportYear())
                                    .setMonth(r.getMonthName())
                                    .setTotalSuccess(r.getTotalSuccess().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawYearStatusSuccess> findYearlyWithdrawStatusSuccessCardNumber(
            pb.withdraw.Withdraw.FindYearWithdrawStatusCardNumber request) {
        YearStatusWithdrawCardNumber req = new YearStatusWithdrawCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return withdrawStatusByCardService.findYearlyStatusSuccessByCard(req)
                .map(apiResp -> {
                    ApiResponseWithdrawYearStatusSuccess.Builder builder = ApiResponseWithdrawYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusSuccessResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setTotalSuccess(r.getTotalSuccess().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawMonthStatusFailed> findMonthlyWithdrawStatusFailedCardNumber(
            pb.withdraw.Withdraw.FindMonthlyWithdrawStatusCardNumber request) {
        MonthStatusWithdrawCardNumber req = new MonthStatusWithdrawCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return withdrawStatusByCardService.findMonthStatusFailedByCard(req)
                .map(apiResp -> {
                    ApiResponseWithdrawMonthStatusFailed.Builder builder = ApiResponseWithdrawMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawMonthStatusFailedResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setMonth(r.getMonthName())
                                            .setTotalFailed(r.getTotalFailed().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseWithdrawYearStatusFailed> findYearlyWithdrawStatusFailedCardNumber(
            pb.withdraw.Withdraw.FindYearWithdrawStatusCardNumber request) {
        YearStatusWithdrawCardNumber req = new YearStatusWithdrawCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return withdrawStatusByCardService.findYearlyStatusFailedByCard(req)
                .map(apiResp -> {
                    ApiResponseWithdrawYearStatusFailed.Builder builder = ApiResponseWithdrawYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (WithdrawResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.withdraw.stats.WithdrawStatsStatus.WithdrawYearStatusFailedResponse.newBuilder()
                                            .setYear(r.getReportYear())
                                            .setTotalFailed(r.getTotalFailed().intValue())
                                            .setTotalAmount(r.getTotalAmount().intValue())
                                            .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

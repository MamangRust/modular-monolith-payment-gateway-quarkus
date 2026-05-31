package com.sanedge.topup.handler;

import com.sanedge.topup.domain.requests.stats.MonthTopupStatus;
import com.sanedge.topup.domain.requests.statsbycard.MonthTopupStatusCardNumber;
import com.sanedge.topup.domain.requests.statsbycard.YearTopupStatusCardNumber;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseMonthStatusSuccess;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusFailed;
import com.sanedge.topup.domain.response.stats.status.TopupResponseYearStatusSuccess;
import com.sanedge.topup.service.stats.status.TopupStatusByCardService;
import com.sanedge.topup.service.stats.status.TopupStatusService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.topup.stats.MutinyTopupStatsStatusServiceGrpc;
import pb.topup.stats.TopupStatsStatus.ApiResponseTopupMonthStatusFailed;
import pb.topup.stats.TopupStatsStatus.ApiResponseTopupMonthStatusSuccess;
import pb.topup.stats.TopupStatsStatus.ApiResponseTopupYearStatusFailed;
import pb.topup.stats.TopupStatsStatus.ApiResponseTopupYearStatusSuccess;

@GrpcService
@Singleton
public class TopupStatsStatusGrpcHandler extends MutinyTopupStatsStatusServiceGrpc.TopupStatsStatusServiceImplBase {

    @Inject
    TopupStatusService topupStatusService;

    @Inject
    TopupStatusByCardService topupStatusByCardService;

    @Override
    public Uni<ApiResponseTopupMonthStatusSuccess> findMonthlyTopupStatusSuccess(
            pb.topup.Topup.FindMonthlyTopupStatus request) {
        MonthTopupStatus req = new MonthTopupStatus();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return topupStatusService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthStatusSuccess.Builder builder = ApiResponseTopupMonthStatusSuccess.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupMonthStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTopupYearStatusSuccess> findYearlyTopupStatusSuccess(
            pb.topup.Topup.FindYearTopupStatus request) {
        return topupStatusService.findYearlyStatusSuccess((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupYearStatusSuccess.Builder builder = ApiResponseTopupYearStatusSuccess.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupYearStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTopupMonthStatusFailed> findMonthlyTopupStatusFailed(
            pb.topup.Topup.FindMonthlyTopupStatus request) {
        MonthTopupStatus req = new MonthTopupStatus();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return topupStatusService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthStatusFailed.Builder builder = ApiResponseTopupMonthStatusFailed.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupMonthStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTopupYearStatusFailed> findYearlyTopupStatusFailed(
            pb.topup.Topup.FindYearTopupStatus request) {
        return topupStatusService.findYearlyStatusFailed((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTopupYearStatusFailed.Builder builder = ApiResponseTopupYearStatusFailed.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupYearStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTopupMonthStatusSuccess> findMonthlyTopupStatusSuccessByCardNumber(
            pb.topup.Topup.FindMonthlyTopupStatusCardNumber request) {
        MonthTopupStatusCardNumber req = new MonthTopupStatusCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return topupStatusByCardService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthStatusSuccess.Builder builder = ApiResponseTopupMonthStatusSuccess.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupMonthStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTopupYearStatusSuccess> findYearlyTopupStatusSuccessByCardNumber(
            pb.topup.Topup.FindYearTopupStatusCardNumber request) {
        YearTopupStatusCardNumber req = new YearTopupStatusCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupStatusByCardService.findYearlyStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTopupYearStatusSuccess.Builder builder = ApiResponseTopupYearStatusSuccess.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupYearStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTopupMonthStatusFailed> findMonthlyTopupStatusFailedByCardNumber(
            pb.topup.Topup.FindMonthlyTopupStatusCardNumber request) {
        MonthTopupStatusCardNumber req = new MonthTopupStatusCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return topupStatusByCardService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTopupMonthStatusFailed.Builder builder = ApiResponseTopupMonthStatusFailed.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupMonthStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTopupYearStatusFailed> findYearlyTopupStatusFailedByCardNumber(
            pb.topup.Topup.FindYearTopupStatusCardNumber request) {
        YearTopupStatusCardNumber req = new YearTopupStatusCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return topupStatusByCardService.findYearlyStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTopupYearStatusFailed.Builder builder = ApiResponseTopupYearStatusFailed.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TopupResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(pb.topup.stats.TopupStatsStatus.TopupYearStatusFailedResponse.newBuilder()
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

package com.sanedge.transfer.handler;

import com.sanedge.transfer.domain.requests.stats.MonthStatusTransfer;
import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;
import com.sanedge.transfer.domain.requests.statsbycard.YearStatusTransferCardNumber;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseMonthStatusSuccess;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusFailed;
import com.sanedge.transfer.domain.response.stats.status.TransferResponseYearStatusSuccess;
import com.sanedge.transfer.service.stats.status.TransferStatusByCardService;
import com.sanedge.transfer.service.stats.status.TransferStatusService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.transfer.stats.MutinyTransferStatsStatusServiceGrpc;
import pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusFailed;
import pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusSuccess;
import pb.transfer.stats.TransferStatsStatus.ApiResponseTransferYearStatusFailed;
import pb.transfer.stats.TransferStatsStatus.ApiResponseTransferYearStatusSuccess;

@GrpcService
@Singleton
public class TransferStatsStatusGrpcHandler
        extends MutinyTransferStatsStatusServiceGrpc.TransferStatsStatusServiceImplBase {

    @Inject
    TransferStatusService transferStatusService;

    @Inject
    TransferStatusByCardService transferStatusByCardService;

    @Override
    public Uni<ApiResponseTransferMonthStatusSuccess> findMonthlyTransferStatusSuccess(
            pb.transfer.Transfer.FindMonthlyTransferStatus request) {
        MonthStatusTransfer req = new MonthStatusTransfer();
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return transferStatusService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthStatusSuccess.Builder builder = ApiResponseTransferMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.transfer.stats.TransferStatsStatus.TransferMonthStatusSuccessResponse
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
    public Uni<ApiResponseTransferYearStatusSuccess> findYearlyTransferStatusSuccess(
            pb.transfer.Transfer.FindYearTransferStatus request) {
        return transferStatusService.findYearlyStatusSuccess((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransferYearStatusSuccess.Builder builder = ApiResponseTransferYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferYearStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTransferMonthStatusFailed> findMonthlyTransferStatusFailed(
            pb.transfer.Transfer.FindMonthlyTransferStatus request) {
        MonthStatusTransfer req = new MonthStatusTransfer();
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return transferStatusService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthStatusFailed.Builder builder = ApiResponseTransferMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferMonthStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTransferYearStatusFailed> findYearlyTransferStatusFailed(
            pb.transfer.Transfer.FindYearTransferStatus request) {
        return transferStatusService.findYearlyStatusFailed((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransferYearStatusFailed.Builder builder = ApiResponseTransferYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferYearStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTransferMonthStatusSuccess> findMonthlyTransferStatusSuccessByCardNumber(
            pb.transfer.Transfer.FindMonthlyTransferStatusCardNumber request) {
        MonthStatusTransferCardNumber req = new MonthStatusTransferCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return transferStatusByCardService.findMonthStatusSuccessByCard(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthStatusSuccess.Builder builder = ApiResponseTransferMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(pb.transfer.stats.TransferStatsStatus.TransferMonthStatusSuccessResponse
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
    public Uni<ApiResponseTransferYearStatusSuccess> findYearlyTransferStatusSuccessByCardNumber(
            pb.transfer.Transfer.FindYearTransferStatusCardNumber request) {
        YearStatusTransferCardNumber req = new YearStatusTransferCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferStatusByCardService.findYearlyStatusSuccessByCard(req)
                .map(apiResp -> {
                    ApiResponseTransferYearStatusSuccess.Builder builder = ApiResponseTransferYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferYearStatusSuccessResponse.newBuilder()
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
    public Uni<ApiResponseTransferMonthStatusFailed> findMonthlyTransferStatusFailedByCardNumber(
            pb.transfer.Transfer.FindMonthlyTransferStatusCardNumber request) {
        MonthStatusTransferCardNumber req = new MonthStatusTransferCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());
        req.setMonth(request.getMonth());

        return transferStatusByCardService.findMonthStatusFailedByCard(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthStatusFailed.Builder builder = ApiResponseTransferMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferMonthStatusFailedResponse.newBuilder()
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
    public Uni<ApiResponseTransferYearStatusFailed> findYearlyTransferStatusFailedByCardNumber(
            pb.transfer.Transfer.FindYearTransferStatusCardNumber request) {
        YearStatusTransferCardNumber req = new YearStatusTransferCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferStatusByCardService.findYearlyStatusFailedByCard(req)
                .map(apiResp -> {
                    ApiResponseTransferYearStatusFailed.Builder builder = ApiResponseTransferYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsStatus.TransferYearStatusFailedResponse.newBuilder()
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

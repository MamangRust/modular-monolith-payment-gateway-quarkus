package com.sanedge.transaction.handler;

import com.sanedge.transaction.domain.requests.stats.MonthStatusTransaction;
import com.sanedge.transaction.domain.requests.statsbycard.MonthStatusTransactionCardNumber;
import com.sanedge.transaction.domain.requests.statsbycard.YearStatusTransactionCardNumber;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseMonthStatusSuccess;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusFailed;
import com.sanedge.transaction.domain.response.stats.status.TransactionResponseYearStatusSuccess;
import com.sanedge.transaction.service.stats.status.TransactionStatusByCardService;
import com.sanedge.transaction.service.stats.status.TransactionStatusService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.transaction.stats.MutinyTransactionStatsStatusServiceGrpc;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusFailed;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusSuccess;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusFailed;
import pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionYearStatusSuccess;

@GrpcService
@Singleton
public class TransactionStatsStatusGrpcHandler
        extends MutinyTransactionStatsStatusServiceGrpc.TransactionStatsStatusServiceImplBase {

    @Inject
    TransactionStatusService transactionStatusService;

    @Inject
    TransactionStatusByCardService transactionStatusByCardService;

    @Override
    public Uni<ApiResponseTransactionMonthStatusSuccess> findMonthlyTransactionStatusSuccess(
            pb.transaction.Transaction.FindMonthlyTransactionStatus request) {
        MonthStatusTransaction req = new MonthStatusTransaction();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return transactionStatusService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthStatusSuccess.Builder builder = ApiResponseTransactionMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusSuccessResponse
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
    public Uni<ApiResponseTransactionYearStatusSuccess> findYearlyTransactionStatusSuccess(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionStatusService.findYearlyStatusSuccess((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionYearStatusSuccess.Builder builder = ApiResponseTransactionYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusSuccessResponse
                                            .newBuilder()
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
    public Uni<ApiResponseTransactionMonthStatusFailed> findMonthlyTransactionStatusFailed(
            pb.transaction.Transaction.FindMonthlyTransactionStatus request) {
        MonthStatusTransaction req = new MonthStatusTransaction();
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return transactionStatusService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthStatusFailed.Builder builder = ApiResponseTransactionMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusFailedResponse
                                            .newBuilder()
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
    public Uni<ApiResponseTransactionYearStatusFailed> findYearlyTransactionStatusFailed(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionStatusService.findYearlyStatusFailed((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionYearStatusFailed.Builder builder = ApiResponseTransactionYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusFailedResponse
                                            .newBuilder()
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
    public Uni<ApiResponseTransactionMonthStatusSuccess> findMonthlyTransactionStatusSuccessByCardNumber(
            pb.transaction.Transaction.FindMonthlyTransactionStatusCardNumber request) {
        MonthStatusTransactionCardNumber req = new MonthStatusTransactionCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return transactionStatusByCardService.findMonthStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthStatusSuccess.Builder builder = ApiResponseTransactionMonthStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseMonthStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusSuccessResponse
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
    public Uni<ApiResponseTransactionYearStatusSuccess> findYearlyTransactionStatusSuccessByCardNumber(
            pb.transaction.Transaction.FindYearTransactionStatusCardNumber request) {
        YearStatusTransactionCardNumber req = new YearStatusTransactionCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionStatusByCardService.findYearlyStatusSuccess(req)
                .map(apiResp -> {
                    ApiResponseTransactionYearStatusSuccess.Builder builder = ApiResponseTransactionYearStatusSuccess
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseYearStatusSuccess r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusSuccessResponse
                                            .newBuilder()
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
    public Uni<ApiResponseTransactionMonthStatusFailed> findMonthlyTransactionStatusFailedByCardNumber(
            pb.transaction.Transaction.FindMonthlyTransactionStatusCardNumber request) {
        MonthStatusTransactionCardNumber req = new MonthStatusTransactionCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());
        req.setMonth(request.getMonth());

        return transactionStatusByCardService.findMonthStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthStatusFailed.Builder builder = ApiResponseTransactionMonthStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseMonthStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionMonthStatusFailedResponse
                                            .newBuilder()
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
    public Uni<ApiResponseTransactionYearStatusFailed> findYearlyTransactionStatusFailedByCardNumber(
            pb.transaction.Transaction.FindYearTransactionStatusCardNumber request) {
        YearStatusTransactionCardNumber req = new YearStatusTransactionCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionStatusByCardService.findYearlyStatusFailed(req)
                .map(apiResp -> {
                    ApiResponseTransactionYearStatusFailed.Builder builder = ApiResponseTransactionYearStatusFailed
                            .newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionResponseYearStatusFailed r : apiResp.data()) {
                            builder.addData(
                                    pb.transaction.stats.TransactionStatsStatus.TransactionYearStatusFailedResponse
                                            .newBuilder()
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

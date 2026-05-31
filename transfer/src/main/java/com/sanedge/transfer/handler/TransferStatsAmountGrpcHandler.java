package com.sanedge.transfer.handler;

import java.util.List;

import com.sanedge.transfer.domain.requests.statsbycard.MonthYearCardNumber;
import com.sanedge.transfer.domain.response.stats.amount.TransferMonthAmountResponse;
import com.sanedge.transfer.domain.response.stats.amount.TransferYearAmountResponse;
import com.sanedge.transfer.service.stats.amount.TransferAmountByCardService;
import com.sanedge.transfer.service.stats.amount.TransferAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.transfer.stats.MutinyTransferStatsAmountServiceGrpc;
import pb.transfer.stats.TransferStatsAmount.ApiResponseTransferMonthAmount;
import pb.transfer.stats.TransferStatsAmount.ApiResponseTransferYearAmount;

@GrpcService
@Singleton
public class TransferStatsAmountGrpcHandler
        extends MutinyTransferStatsAmountServiceGrpc.TransferStatsAmountServiceImplBase {

    @Inject
    TransferAmountService transferAmountService;

    @Inject
    TransferAmountByCardService transferAmountByCardService;

    @Override
    public Uni<ApiResponseTransferMonthAmount> findMonthlyTransferAmounts(
            pb.transfer.Transfer.FindYearTransferStatus request) {
        return transferAmountService.findMonthlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransferMonthAmount.Builder builder = ApiResponseTransferMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferMonthAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferMonthAmountResponse.newBuilder()
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
    public Uni<ApiResponseTransferYearAmount> findYearlyTransferAmounts(
            pb.transfer.Transfer.FindYearTransferStatus request) {
        return transferAmountService.findYearlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransferYearAmount.Builder builder = ApiResponseTransferYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferYearAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferYearAmountResponse.newBuilder()
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
    public Uni<ApiResponseTransferMonthAmount> findMonthlyTransferAmountsBySenderCardNumber(
            pb.transfer.Transfer.FindByCardNumberTransferRequest request) {
        MonthYearCardNumber req = new MonthYearCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferAmountByCardService.findMonthlyAmountsBySender(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthAmount.Builder builder = ApiResponseTransferMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferMonthAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferMonthAmountResponse.newBuilder()
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
    public Uni<ApiResponseTransferMonthAmount> findMonthlyTransferAmountsByReceiverCardNumber(
            pb.transfer.Transfer.FindByCardNumberTransferRequest request) {
        MonthYearCardNumber req = new MonthYearCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferAmountByCardService.findMonthlyAmountsByReceiver(req)
                .map(apiResp -> {
                    ApiResponseTransferMonthAmount.Builder builder = ApiResponseTransferMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferMonthAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferMonthAmountResponse.newBuilder()
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
    public Uni<ApiResponseTransferYearAmount> findYearlyTransferAmountsBySenderCardNumber(
            pb.transfer.Transfer.FindByCardNumberTransferRequest request) {
        MonthYearCardNumber req = new MonthYearCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferAmountByCardService.findYearlyAmountsBySender(req)
                .map(apiResp -> {
                    ApiResponseTransferYearAmount.Builder builder = ApiResponseTransferYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferYearAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferYearAmountResponse.newBuilder()
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
    public Uni<ApiResponseTransferYearAmount> findYearlyTransferAmountsByReceiverCardNumber(
            pb.transfer.Transfer.FindByCardNumberTransferRequest request) {
        MonthYearCardNumber req = new MonthYearCardNumber();
        req.setCardNumber(request.getCardNumber());
        req.setYear(request.getYear());

        return transferAmountByCardService.findYearlyAmountsByReceiver(req)
                .map(apiResp -> {
                    ApiResponseTransferYearAmount.Builder builder = ApiResponseTransferYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransferYearAmountResponse r : apiResp.data()) {
                            builder.addData(
                                    pb.transfer.stats.TransferStatsAmount.TransferYearAmountResponse.newBuilder()
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

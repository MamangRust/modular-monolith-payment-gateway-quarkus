package com.sanedge.transaction.handler;

import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.transaction.domain.response.stats.amount.TransactionMonthAmountResponse;
import com.sanedge.transaction.domain.response.stats.amount.TransactionYearlyAmountResponse;
import com.sanedge.transaction.service.stats.amount.TransactionAmountByCardService;
import com.sanedge.transaction.service.stats.amount.TransactionAmountService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.transaction.stats.MutinyTransactionStatsAmountServiceGrpc;
import pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionMonthAmount;
import pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionYearAmount;

@GrpcService
@Singleton
public class TransactionStatsAmountGrpcHandler
        extends MutinyTransactionStatsAmountServiceGrpc.TransactionStatsAmountServiceImplBase {

    @Inject
    TransactionAmountService transactionAmountService;

    @Inject
    TransactionAmountByCardService transactionAmountByCardService;

    @Override
    public Uni<ApiResponseTransactionMonthAmount> findMonthlyAmounts(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionAmountService.findMonthlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionMonthAmount.Builder builder = ApiResponseTransactionMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionMonthAmountResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsAmount.TransactionMonthAmountResponse
                                    .newBuilder()
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
    public Uni<ApiResponseTransactionYearAmount> findYearlyAmounts(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionAmountService.findYearlyAmounts((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionYearAmount.Builder builder = ApiResponseTransactionYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsAmount.TransactionYearlyAmountResponse
                                    .newBuilder()
                                    .setYear(r.getYear())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTransactionMonthAmount> findMonthlyAmountsByCardNumber(
            pb.transaction.Transaction.FindByYearCardNumberTransactionRequest request) {
        MonthYearPaymentMethod req = new MonthYearPaymentMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionAmountByCardService.findMonthlyAmounts(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthAmount.Builder builder = ApiResponseTransactionMonthAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionMonthAmountResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsAmount.TransactionMonthAmountResponse
                                    .newBuilder()
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
    public Uni<ApiResponseTransactionYearAmount> findYearlyAmountsByCardNumber(
            pb.transaction.Transaction.FindByYearCardNumberTransactionRequest request) {
        MonthYearPaymentMethod req = new MonthYearPaymentMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionAmountByCardService.findYearlyAmounts(req)
                .map(apiResp -> {
                    ApiResponseTransactionYearAmount.Builder builder = ApiResponseTransactionYearAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionYearlyAmountResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsAmount.TransactionYearlyAmountResponse
                                    .newBuilder()
                                    .setYear(r.getYear())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

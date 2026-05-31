package com.sanedge.transaction.handler;

import java.util.List;

import com.sanedge.transaction.domain.requests.stats.MonthYearPaymentMethod;
import com.sanedge.transaction.domain.response.stats.method.TransactionMonthMethodResponse;
import com.sanedge.transaction.domain.response.stats.method.TransactionYearMethodResponse;
import com.sanedge.transaction.service.stats.method.TransactionMethodByCardService;
import com.sanedge.transaction.service.stats.method.TransactionMethodService;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import pb.transaction.stats.MutinyTransactionStatsMethodServiceGrpc;
import pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionMonthMethod;
import pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionYearMethod;

@GrpcService
@Singleton
public class TransactionStatsMethodGrpcHandler
        extends MutinyTransactionStatsMethodServiceGrpc.TransactionStatsMethodServiceImplBase {

    @Inject
    TransactionMethodService transactionMethodService;

    @Inject
    TransactionMethodByCardService transactionMethodByCardService;

    @Override
    public Uni<ApiResponseTransactionMonthMethod> findMonthlyPaymentMethods(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionMethodService.findMonthlyMethod((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionMonthMethod.Builder builder = ApiResponseTransactionMonthMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionMonthMethodResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsMethod.TransactionMonthMethodResponse
                                    .newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setPaymentMethod(r.getPaymentMethod())
                                    .setTotalTransactions(r.getTotalTransactions().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTransactionYearMethod> findYearlyPaymentMethods(
            pb.transaction.Transaction.FindYearTransactionStatus request) {
        return transactionMethodService.findYearlyMethod((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseTransactionYearMethod.Builder builder = ApiResponseTransactionYearMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionYearMethodResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsMethod.TransactionYearMethodResponse
                                    .newBuilder()
                                    .setYear(r.getReportYear())
                                    .setPaymentMethod(r.getPaymentMethod())
                                    .setTotalTransactions(r.getTotalTransactions().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTransactionMonthMethod> findMonthlyPaymentMethodsByCardNumber(
            pb.transaction.Transaction.FindByYearCardNumberTransactionRequest request) {
        MonthYearPaymentMethod req = new MonthYearPaymentMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionMethodByCardService.findMonthlyMethod(req)
                .map(apiResp -> {
                    ApiResponseTransactionMonthMethod.Builder builder = ApiResponseTransactionMonthMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionMonthMethodResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsMethod.TransactionMonthMethodResponse
                                    .newBuilder()
                                    .setMonth(r.getMonthName())
                                    .setPaymentMethod(r.getPaymentMethod())
                                    .setTotalTransactions(r.getTotalTransactions().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseTransactionYearMethod> findYearlyPaymentMethodsByCardNumber(
            pb.transaction.Transaction.FindByYearCardNumberTransactionRequest request) {
        MonthYearPaymentMethod req = new MonthYearPaymentMethod();
        req.setCardNumber(request.getCardNumber());
        req.setYear((long) request.getYear());

        return transactionMethodByCardService.findYearlyMethod(req)
                .map(apiResp -> {
                    ApiResponseTransactionYearMethod.Builder builder = ApiResponseTransactionYearMethod.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (TransactionYearMethodResponse r : apiResp.data()) {
                            builder.addData(pb.transaction.stats.TransactionStatsMethod.TransactionYearMethodResponse
                                    .newBuilder()
                                    .setYear(r.getReportYear())
                                    .setPaymentMethod(r.getPaymentMethod())
                                    .setTotalTransactions(r.getTotalTransactions().intValue())
                                    .setTotalAmount(r.getTotalAmount().intValue())
                                    .build());
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

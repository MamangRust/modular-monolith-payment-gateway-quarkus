package com.sanedge.card.handler;

import com.sanedge.card.service.stats.CardTransferAmountService;
import com.sanedge.card.service.statsbycard.CardTransferAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import pb.card.stats.MutinyCardStatsTransferServiceGrpc;
import pb.card.Card.FindYearAmount;
import pb.card.Card.FindYearAmountCardNumber;
import pb.card.Card.ApiResponseMonthlyAmount;
import pb.card.Card.ApiResponseYearlyAmount;
import pb.card.Card.CardResponseMonthlyAmount;
import pb.card.Card.CardResponseYearlyAmount;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@GrpcService
@Singleton
public class CardStatsTransferGrpcHandler extends MutinyCardStatsTransferServiceGrpc.CardStatsTransferServiceImplBase {

    @Inject
    CardTransferAmountService cardTransferAmountService;

    @Inject
    CardTransferAmountByCardService cardTransferAmountByCardService;

    @Override
    public Uni<ApiResponseMonthlyAmount> findMonthlyTransferSenderAmount(FindYearAmount request) {
        return cardTransferAmountService.findMonthAmountSender((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMonthlyAmount.Builder builder = ApiResponseMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthAmount ma : apiResp.data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyAmount> findYearlyTransferSenderAmount(FindYearAmount request) {
        return cardTransferAmountService.findYearAmountSender((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseYearlyAmount.Builder builder = ApiResponseYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearAmount ya : apiResp.data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMonthlyAmount> findMonthlyTransferReceiverAmount(FindYearAmount request) {
        return cardTransferAmountService.findMonthAmountReceiver((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMonthlyAmount.Builder builder = ApiResponseMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthAmount ma : apiResp.data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyAmount> findYearlyTransferReceiverAmount(FindYearAmount request) {
        return cardTransferAmountService.findYearAmountReceiver((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseYearlyAmount.Builder builder = ApiResponseYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearAmount ya : apiResp.data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMonthlyAmount> findMonthlyTransferSenderAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardTransferAmountByCardService.findMonthAmountSender(domainReq)
                .map(apiResp -> {
                    ApiResponseMonthlyAmount.Builder builder = ApiResponseMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthAmount ma : apiResp.data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyAmount> findYearlyTransferSenderAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardTransferAmountByCardService.findYearAmountSender(domainReq)
                .map(apiResp -> {
                    ApiResponseYearlyAmount.Builder builder = ApiResponseYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearAmount ya : apiResp.data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMonthlyAmount> findMonthlyTransferReceiverAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardTransferAmountByCardService.findMonthAmountReceiver(domainReq)
                .map(apiResp -> {
                    ApiResponseMonthlyAmount.Builder builder = ApiResponseMonthlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthAmount ma : apiResp.data()) {
                            builder.addData(toProto(ma));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyAmount> findYearlyTransferReceiverAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardTransferAmountByCardService.findYearAmountReceiver(domainReq)
                .map(apiResp -> {
                    ApiResponseYearlyAmount.Builder builder = ApiResponseYearlyAmount.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearAmount ya : apiResp.data()) {
                            builder.addData(toProto(ya));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private CardResponseMonthlyAmount toProto(CardResponseMonthAmount m) {
        if (m == null) {
            return CardResponseMonthlyAmount.getDefaultInstance();
        }
        CardResponseMonthlyAmount.Builder b = CardResponseMonthlyAmount.newBuilder();
        if (m.getMonthName() != null) b.setMonth(m.getMonthName());
        if (m.getTotalAmount() != null) b.setTotalAmount(m.getTotalAmount());
        return b.build();
    }

    private CardResponseYearlyAmount toProto(CardResponseYearAmount y) {
        if (y == null) {
            return CardResponseYearlyAmount.getDefaultInstance();
        }
        CardResponseYearlyAmount.Builder b = CardResponseYearlyAmount.newBuilder();
        if (y.getReportYear() != null) b.setYear(y.getReportYear());
        if (y.getTotalAmount() != null) b.setTotalAmount(y.getTotalAmount());
        return b.build();
    }
}

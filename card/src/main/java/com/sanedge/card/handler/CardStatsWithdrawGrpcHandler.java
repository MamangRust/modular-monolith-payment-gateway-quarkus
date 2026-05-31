package com.sanedge.card.handler;

import com.sanedge.card.service.stats.CardWithdrawAmountService;
import com.sanedge.card.service.statsbycard.CardWithdrawAmountByCardService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.card.domain.response.stats.amount.CardResponseMonthAmount;
import com.sanedge.card.domain.response.stats.amount.CardResponseYearAmount;

import pb.card.stats.MutinyCardStatsWithdrawServiceGrpc;
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
public class CardStatsWithdrawGrpcHandler extends MutinyCardStatsWithdrawServiceGrpc.CardStatsWithdrawServiceImplBase {

    @Inject
    CardWithdrawAmountService cardWithdrawAmountService;

    @Inject
    CardWithdrawAmountByCardService cardWithdrawAmountByCardService;

    @Override
    public Uni<ApiResponseMonthlyAmount> findMonthlyWithdrawAmount(FindYearAmount request) {
        return cardWithdrawAmountService.findMonthAmount((long) request.getYear())
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
    public Uni<ApiResponseYearlyAmount> findYearlyWithdrawAmount(FindYearAmount request) {
        return cardWithdrawAmountService.findYearAmount((long) request.getYear())
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
    public Uni<ApiResponseMonthlyAmount> findMonthlyWithdrawAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardWithdrawAmountByCardService.findMonthAmountByCard(domainReq)
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
    public Uni<ApiResponseYearlyAmount> findYearlyWithdrawAmountByCardNumber(FindYearAmountCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardWithdrawAmountByCardService.findYearAmountByCard(domainReq)
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

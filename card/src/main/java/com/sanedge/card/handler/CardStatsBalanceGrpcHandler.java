package com.sanedge.card.handler;

import com.sanedge.card.service.stats.CardBalanceService;
import com.sanedge.card.service.statsbycard.CardBalanceByCardService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.card.domain.requests.MonthYearCardNumberCard;
import com.sanedge.card.domain.response.stats.balance.CardResponseMonthBalance;
import com.sanedge.card.domain.response.stats.balance.CardResponseYearBalance;

import pb.card.stats.MutinyCardStatsBalanceServiceGrpc;
import pb.card.stats.CardStatsBalance.FindYearBalance;
import pb.card.stats.CardStatsBalance.FindYearBalanceCardNumber;
import pb.card.stats.CardStatsBalance.ApiResponseMonthlyBalance;
import pb.card.stats.CardStatsBalance.ApiResponseYearlyBalance;
import pb.card.stats.CardStatsBalance.CardResponseMonthlyBalance;
import pb.card.stats.CardStatsBalance.CardResponseYearlyBalance;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@GrpcService
@Singleton
public class CardStatsBalanceGrpcHandler extends MutinyCardStatsBalanceServiceGrpc.CardStatsBalanceServiceImplBase {

    @Inject
    CardBalanceService cardBalanceService;

    @Inject
    CardBalanceByCardService cardBalanceByCardService;

    @Override
    public Uni<ApiResponseMonthlyBalance> findMonthlyBalance(FindYearBalance request) {
        return cardBalanceService.findMonthBalance((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseMonthlyBalance.Builder builder = ApiResponseMonthlyBalance.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthBalance mb : apiResp.data()) {
                            builder.addData(toProto(mb));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyBalance> findYearlyBalance(FindYearBalance request) {
        return cardBalanceService.findYearBalance((long) request.getYear())
                .map(apiResp -> {
                    ApiResponseYearlyBalance.Builder builder = ApiResponseYearlyBalance.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearBalance yb : apiResp.data()) {
                            builder.addData(toProto(yb));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseMonthlyBalance> findMonthlyBalanceByCardNumber(FindYearBalanceCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardBalanceByCardService.findMonthBalanceByCard(domainReq)
                .map(apiResp -> {
                    ApiResponseMonthlyBalance.Builder builder = ApiResponseMonthlyBalance.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseMonthBalance mb : apiResp.data()) {
                            builder.addData(toProto(mb));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseYearlyBalance> findYearlyBalanceByCardNumber(FindYearBalanceCardNumber request) {
        MonthYearCardNumberCard domainReq = new MonthYearCardNumberCard(request.getCardNumber(), (long) request.getYear());
        return cardBalanceByCardService.findYearBalanceByCard(domainReq)
                .map(apiResp -> {
                    ApiResponseYearlyBalance.Builder builder = ApiResponseYearlyBalance.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (CardResponseYearBalance yb : apiResp.data()) {
                            builder.addData(toProto(yb));
                        }
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private CardResponseMonthlyBalance toProto(CardResponseMonthBalance m) {
        if (m == null) {
            return CardResponseMonthlyBalance.getDefaultInstance();
        }
        CardResponseMonthlyBalance.Builder b = CardResponseMonthlyBalance.newBuilder();
        if (m.getMonth() != null) b.setMonth(m.getMonth());
        if (m.getTotalBalance() != null) b.setTotalBalance(m.getTotalBalance());
        return b.build();
    }

    private CardResponseYearlyBalance toProto(CardResponseYearBalance y) {
        if (y == null) {
            return CardResponseYearlyBalance.getDefaultInstance();
        }
        CardResponseYearlyBalance.Builder b = CardResponseYearlyBalance.newBuilder();
        if (y.getReportYear() != null) b.setYear(y.getReportYear());
        if (y.getTotalBalance() != null) b.setTotalBalance(y.getTotalBalance());
        return b.build();
    }
}

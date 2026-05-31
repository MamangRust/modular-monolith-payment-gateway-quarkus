package com.sanedge.card.handler;

import com.sanedge.card.service.CardDashboardService;
import com.sanedge.common.domain.response.ApiResponse;

import pb.card.MutinyCardDashboardServiceGrpc;
import pb.card.Card.FindByCardNumberRequest;
import pb.card.CardDashboard.ApiResponseDashboardCard;
import pb.card.CardDashboard.ApiResponseDashboardCardNumber;
import pb.card.CardDashboard.CardResponseDashboard;
import pb.card.CardDashboard.CardResponseDashboardCardNumber;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import com.google.protobuf.Empty;

@GrpcService
@Singleton
public class CardDashboardGrpcHandler extends MutinyCardDashboardServiceGrpc.CardDashboardServiceImplBase {

    @Inject
    CardDashboardService cardDashboardService;

    @Override
    public Uni<ApiResponseDashboardCard> dashboardCard(Empty request) {
        return cardDashboardService.dashboard()
                .map(apiResp -> {
                    ApiResponseDashboardCard.Builder builder = ApiResponseDashboardCard.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        com.sanedge.card.domain.response.dashboard.CardDashboard d = apiResp.data();
                        CardResponseDashboard.Builder dataBuilder = CardResponseDashboard.newBuilder();
                        if (d.getTotalBalance() != null) dataBuilder.setTotalBalance(d.getTotalBalance());
                        if (d.getTotalTopup() != null) dataBuilder.setTotalTopup(d.getTotalTopup());
                        if (d.getTotalWithdraw() != null) dataBuilder.setTotalWithdraw(d.getTotalWithdraw());
                        if (d.getTotalTransaction() != null) dataBuilder.setTotalTransaction(d.getTotalTransaction());
                        if (d.getTotalTransfer() != null) dataBuilder.setTotalTransfer(d.getTotalTransfer());
                        builder.setData(dataBuilder.build());
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponseDashboardCardNumber> dashboardCardNumber(FindByCardNumberRequest request) {
        return cardDashboardService.dashboardByCard(request.getCardNumber())
                .map(apiResp -> {
                    ApiResponseDashboardCardNumber.Builder builder = ApiResponseDashboardCardNumber.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        com.sanedge.card.domain.response.dashboard.CardDashboardCard d = apiResp.data();
                        CardResponseDashboardCardNumber.Builder dataBuilder = CardResponseDashboardCardNumber.newBuilder();
                        if (d.getTotalBalance() != null) dataBuilder.setTotalBalance(d.getTotalBalance());
                        if (d.getTotalTopup() != null) dataBuilder.setTotalTopup(d.getTotalTopup());
                        if (d.getTotalWithdraw() != null) dataBuilder.setTotalWithdraw(d.getTotalWithdraw());
                        if (d.getTotalTransaction() != null) dataBuilder.setTotalTransaction(d.getTotalTransaction());
                        if (d.getTotalTransferSend() != null) dataBuilder.setTotalTransferSend(d.getTotalTransferSend());
                        if (d.getTotalTransferReceiver() != null) dataBuilder.setTotalTransferReceiver(d.getTotalTransferReceiver());
                        builder.setData(dataBuilder.build());
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }
}

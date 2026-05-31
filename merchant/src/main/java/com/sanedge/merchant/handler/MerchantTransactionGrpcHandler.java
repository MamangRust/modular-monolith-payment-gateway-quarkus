package com.sanedge.merchant.handler;

import com.sanedge.merchant.service.MerchantTransactionService;
import com.sanedge.common.domain.response.ApiResponsePagination;
import com.sanedge.merchant.domain.response.MerchantTransactionResponse;

import pb.merchant.MutinyMerchantTransactionServiceGrpc;
import pb.merchant.Merchant.FindAllMerchantTransaction;
import pb.merchant.Merchant.FindAllMerchantTransactionId;
import pb.merchant.Merchant.FindAllMerchantTransactionApikey;
import pb.merchant.MerchantTransaction.ApiResponsePaginationMerchantTransaction;

import io.grpc.Status;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

@GrpcService
@Singleton
public class MerchantTransactionGrpcHandler extends MutinyMerchantTransactionServiceGrpc.MerchantTransactionServiceImplBase {

    @Inject
    MerchantTransactionService merchantTransactionService;

    @Override
    public Uni<ApiResponsePaginationMerchantTransaction> findAllTransactionMerchant(FindAllMerchantTransaction request) {
        com.sanedge.merchant.domain.requests.FindAllMerchants domainReq = new com.sanedge.merchant.domain.requests.FindAllMerchants();
        domainReq.setPage(request.getPage());
        domainReq.setPageSize(request.getPageSize());
        domainReq.setSearch(request.getSearch());

        return merchantTransactionService.findAll(domainReq)
                .map(apiResp -> {
                    ApiResponsePaginationMerchantTransaction.Builder builder = ApiResponsePaginationMerchantTransaction.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (MerchantTransactionResponse tr : apiResp.data()) {
                            builder.addData(toProto(tr));
                        }
                    }
                    if (apiResp.pagination() != null) {
                        builder.setPaginationMeta(toProto(apiResp.pagination()));
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponsePaginationMerchantTransaction> findAllTransactionByApikey(FindAllMerchantTransactionApikey request) {
        com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey domainReq = new com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey();
        domainReq.setApiKey(request.getApiKey());
        domainReq.setPage(request.getPage());
        domainReq.setPageSize(request.getPageSize());
        domainReq.setSearch(request.getSearch());

        return merchantTransactionService.findByApiKey(domainReq)
                .map(apiResp -> {
                    ApiResponsePaginationMerchantTransaction.Builder builder = ApiResponsePaginationMerchantTransaction.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (MerchantTransactionResponse tr : apiResp.data()) {
                            builder.addData(toProto(tr));
                        }
                    }
                    if (apiResp.pagination() != null) {
                        builder.setPaginationMeta(toProto(apiResp.pagination()));
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    @Override
    public Uni<ApiResponsePaginationMerchantTransaction> findAllTransactionByMerchant(FindAllMerchantTransactionId request) {
        com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById domainReq = new com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById();
        if (request.getId() != null && !request.getId().isEmpty()) {
            try {
                domainReq.setMerchantId(Long.parseLong(request.getId()));
            } catch (NumberFormatException e) {
                return Uni.createFrom().failure(Status.INVALID_ARGUMENT.withDescription("Invalid merchant ID format").asRuntimeException());
            }
        }
        domainReq.setPage(request.getPage());
        domainReq.setPageSize(request.getPageSize());
        domainReq.setSearch(request.getSearch());

        return merchantTransactionService.findById(domainReq)
                .map(apiResp -> {
                    ApiResponsePaginationMerchantTransaction.Builder builder = ApiResponsePaginationMerchantTransaction.newBuilder()
                            .setStatus(apiResp.status())
                            .setMessage(apiResp.message());
                    if (apiResp.data() != null) {
                        for (MerchantTransactionResponse tr : apiResp.data()) {
                            builder.addData(toProto(tr));
                        }
                    }
                    if (apiResp.pagination() != null) {
                        builder.setPaginationMeta(toProto(apiResp.pagination()));
                    }
                    return builder.build();
                })
                .onFailure().transform(e -> Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
    }

    private pb.merchant.MerchantTransaction.MerchantTransactionResponse toProto(MerchantTransactionResponse r) {
        if (r == null) {
            return pb.merchant.MerchantTransaction.MerchantTransactionResponse.getDefaultInstance();
        }
        pb.merchant.MerchantTransaction.MerchantTransactionResponse.Builder builder = pb.merchant.MerchantTransaction.MerchantTransactionResponse.newBuilder();
        if (r.getId() != null) {
            builder.setId(r.getId().intValue());
        }
        if (r.getCardNumber() != null) {
            builder.setCardNumber(r.getCardNumber());
        }
        if (r.getAmount() != null) {
            builder.setAmount(r.getAmount().intValue());
        }
        if (r.getPaymentMethod() != null) {
            builder.setPaymentMethod(r.getPaymentMethod());
        }
        if (r.getMerchantId() != null) {
            builder.setMerchantId(r.getMerchantId().intValue());
        }
        if (r.getMerchantName() != null) {
            builder.setMerchantName(r.getMerchantName());
        }
        if (r.getTransactionTime() != null) {
            builder.setTransactionTime(r.getTransactionTime());
        }
        if (r.getCreatedAt() != null) {
            builder.setCreatedAt(r.getCreatedAt());
        }
        if (r.getUpdatedAt() != null) {
            builder.setUpdatedAt(r.getUpdatedAt());
        }
        if (r.getDeletedAt() != null) {
            builder.setDeletedAt(com.google.protobuf.StringValue.of(r.getDeletedAt()));
        }
        return builder.build();
    }

    private pb.common.PaginationMeta toProto(com.sanedge.common.domain.response.PaginationMeta m) {
        if (m == null) {
            return pb.common.PaginationMeta.getDefaultInstance();
        }
        return pb.common.PaginationMeta.newBuilder()
                .setCurrentPage(m.currentPage())
                .setPageSize(m.pageSize())
                .setTotalPages(m.totalPages())
                .setTotalRecords(m.totalRecords())
                .build();
    }
}

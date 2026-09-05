package com.sanedge.merchant.service;

import java.util.List;

import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactions;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.common.domain.response.ApiResponsePagination;
import com.sanedge.merchant.domain.response.MerchantTransactionResponse;

import io.smallrye.mutiny.Uni;

public interface MerchantTransactionService {
    Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findAll(FindAllMerchantTransactions req);

    Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findById(FindAllMerchantTransactionsById req);

    Uni<ApiResponsePagination<List<MerchantTransactionResponse>>> findByApiKey(FindAllMerchantTransactionsByApiKey req);
}

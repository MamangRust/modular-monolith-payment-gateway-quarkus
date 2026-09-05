package com.sanedge.gateway.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.TransactionDto;
import com.sanedge.gateway.service.TransactionService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class TransactionResourceTest {

    @Mock private TransactionService transactionService;
    private TransactionResource transactionResource;

    @BeforeEach
    void setUp() throws Exception {
        transactionResource = new TransactionResource();
        Field f = TransactionResource.class.getDeclaredField("transactionService");
        f.setAccessible(true); f.set(transactionResource, transactionService);
    }

    @Test void listTransactions_Success() {
        TransactionDto.ApiResponsePaginationTransaction dto = new TransactionDto.ApiResponsePaginationTransaction("success", "ok", List.of(), null);
        lenient().when(transactionService.listTransactions(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transactionResource.listTransactions(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getTransaction_Success() {
        TransactionDto.ApiResponseTransaction dto = new TransactionDto.ApiResponseTransaction("success", "ok", null);
        lenient().when(transactionService.getTransaction(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transactionResource.getTransaction(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createTransaction_Success_Returns201() {
        TransactionDto.ApiResponseTransaction dto = new TransactionDto.ApiResponseTransaction("success", "ok", null);
        lenient().when(transactionService.createTransaction(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transactionResource.createTransaction(new TransactionDto.CreateRequest("key", "123", 50000, "CSH", 1)).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findMonthlyAmounts_Success() {
        TransactionDto.ApiResponseTransactionMonthAmount dto = new TransactionDto.ApiResponseTransactionMonthAmount("success", "ok", null);
        lenient().when(transactionService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transactionResource.findMonthlyAmounts(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

package com.sanedge.gateway.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.TransactionDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TelemetryHelper telemetryHelper;
    @Mock private pb.transaction.MutinyTransactionQueryServiceGrpc.MutinyTransactionQueryServiceStub transactionQueryService;
    @Mock private pb.transaction.MutinyTransactionCommandServiceGrpc.MutinyTransactionCommandServiceStub transactionCommandService;
    @Mock private pb.transaction.stats.MutinyTransactionStatsAmountServiceGrpc.MutinyTransactionStatsAmountServiceStub transactionStatsAmountService;
    @Mock private pb.transaction.stats.MutinyTransactionStatsMethodServiceGrpc.MutinyTransactionStatsMethodServiceStub transactionStatsMethodService;
    @Mock private pb.transaction.stats.MutinyTransactionStatsStatusServiceGrpc.MutinyTransactionStatsStatusServiceStub transactionStatsStatusService;

    private TransactionServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> { Supplier<Uni<?>> s = inv.getArgument(1); return s.get(); });
        service = new TransactionServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("transactionQueryService", transactionQueryService);
        inject("transactionCommandService", transactionCommandService);
        inject("transactionStatsAmountService", transactionStatsAmountService);
        inject("transactionStatsMethodService", transactionStatsMethodService);
        inject("transactionStatsStatusService", transactionStatsStatusService);
    }

    private void inject(String n, Object v) throws Exception {
        Field f = TransactionServiceImpl.class.getDeclaredField(n);
        f.setAccessible(true); f.set(service, v);
    }

    @Test void listTransactions_PropagatesResponse() {
        pb.transaction.TransactionQuery.ApiResponsePaginationTransaction proto = pb.transaction.TransactionQuery.ApiResponsePaginationTransaction.newBuilder().setStatus("success").build();
        lenient().when(transactionQueryService.findAllTransaction(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.listTransactions(1, 10, "").await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getTransaction_PropagatesResponse() {
        pb.transaction.Transaction.ApiResponseTransaction proto = pb.transaction.Transaction.ApiResponseTransaction.newBuilder().setStatus("success").build();
        lenient().when(transactionQueryService.findByIdTransaction(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getTransaction(1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void createTransaction_PropagatesResponse() {
        TransactionDto.CreateRequest req = new TransactionDto.CreateRequest("api-key", "1234567890", 50000, "CASHLESS", 1);
        pb.transaction.Transaction.ApiResponseTransaction proto = pb.transaction.Transaction.ApiResponseTransaction.newBuilder().setStatus("success").setMessage("created").build();
        lenient().when(transactionCommandService.createTransaction(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.createTransaction(req).await().indefinitely().message()).isEqualTo("created");
    }

    @Test void findMonthlyAmounts_PropagatesResponse() {
        pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionMonthAmount proto = pb.transaction.stats.TransactionStatsAmount.ApiResponseTransactionMonthAmount.newBuilder().setStatus("success").build();
        lenient().when(transactionStatsAmountService.findMonthlyAmounts(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyAmounts(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void findMonthlyMethods_PropagatesResponse() {
        pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionMonthMethod proto = pb.transaction.stats.TransactionStatsMethod.ApiResponseTransactionMonthMethod.newBuilder().setStatus("success").build();
        lenient().when(transactionStatsMethodService.findMonthlyPaymentMethods(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyMethods(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void findMonthlyStatusSuccess_PropagatesResponse() {
        pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusSuccess proto = pb.transaction.stats.TransactionStatsStatus.ApiResponseTransactionMonthStatusSuccess.newBuilder().setStatus("success").build();
        lenient().when(transactionStatsStatusService.findMonthlyTransactionStatusSuccess(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyStatusSuccess(2024, 6).await().indefinitely().status()).isEqualTo("success");
    }
}

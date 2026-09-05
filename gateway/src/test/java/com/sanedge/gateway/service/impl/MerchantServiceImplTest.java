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

import com.sanedge.gateway.dto.MerchantDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantServiceImplTest {

    @Mock private TelemetryHelper telemetryHelper;
    @Mock private pb.merchant.MutinyMerchantQueryServiceGrpc.MutinyMerchantQueryServiceStub merchantQueryService;
    @Mock private pb.merchant.MutinyMerchantCommandServiceGrpc.MutinyMerchantCommandServiceStub merchantCommandService;
    @Mock private pb.merchant.MutinyMerchantTransactionServiceGrpc.MutinyMerchantTransactionServiceStub merchantTransactionService;
    @Mock private pb.merchant.stats.MutinyMerchantStatsAmountServiceGrpc.MutinyMerchantStatsAmountServiceStub merchantStatsAmountService;
    @Mock private pb.merchant.stats.MutinyMerchantStatsMethodServiceGrpc.MutinyMerchantStatsMethodServiceStub merchantStatsMethodService;
    @Mock private pb.merchant.stats.MutinyMerchantStatsTotalAmountServiceGrpc.MutinyMerchantStatsTotalAmountServiceStub merchantStatsTotalAmountService;

    private MerchantServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> { Supplier<Uni<?>> s = inv.getArgument(1); return s.get(); });
        service = new MerchantServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("merchantQueryService", merchantQueryService);
        inject("merchantCommandService", merchantCommandService);
        inject("merchantTransactionService", merchantTransactionService);
        inject("merchantStatsAmountService", merchantStatsAmountService);
        inject("merchantStatsMethodService", merchantStatsMethodService);
        inject("merchantStatsTotalAmountService", merchantStatsTotalAmountService);
    }

    private void inject(String n, Object v) throws Exception {
        Field f = MerchantServiceImpl.class.getDeclaredField(n);
        f.setAccessible(true); f.set(service, v);
    }

    @Test void listMerchants_PropagatesResponse() {
        pb.merchant.MerchantQuery.ApiResponsePaginationMerchant proto = pb.merchant.MerchantQuery.ApiResponsePaginationMerchant.newBuilder().setStatus("success").build();
        lenient().when(merchantQueryService.findAllMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.listMerchants(1, 10, "").await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMerchant_PropagatesResponse() {
        pb.merchant.Merchant.ApiResponseMerchant proto = pb.merchant.Merchant.ApiResponseMerchant.newBuilder().setStatus("success").build();
        lenient().when(merchantQueryService.findByIdMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMerchant(1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void createMerchant_PropagatesResponse() {
        MerchantDto.CreateRequest req = new MerchantDto.CreateRequest("Test Merchant", 1);
        pb.merchant.Merchant.ApiResponseMerchant proto = pb.merchant.Merchant.ApiResponseMerchant.newBuilder().setStatus("success").setMessage("created").build();
        lenient().when(merchantCommandService.createMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.createMerchant(req).await().indefinitely().message()).isEqualTo("created");
    }

    @Test void findAllTransactions_PropagatesResponse() {
        pb.merchant.MerchantTransaction.ApiResponsePaginationMerchantTransaction proto = pb.merchant.MerchantTransaction.ApiResponsePaginationMerchantTransaction.newBuilder().setStatus("success").build();
        lenient().when(merchantTransactionService.findAllTransactionMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findAllTransactions(1, 10, "", 1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMonthlyAmount_PropagatesResponse() {
        pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantMonthlyAmount proto = pb.merchant.stats.MerchantStatsAmount.ApiResponseMerchantMonthlyAmount.newBuilder().setStatus("success").build();
        lenient().when(merchantStatsAmountService.findMonthlyAmountMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyAmount(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMonthlyMethod_PropagatesResponse() {
        pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod proto = pb.merchant.stats.MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod.newBuilder().setStatus("success").build();
        lenient().when(merchantStatsMethodService.findMonthlyPaymentMethodsMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyMethod(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getMonthlyTotalAmount_PropagatesResponse() {
        pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount proto = pb.merchant.stats.MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount.newBuilder().setStatus("success").build();
        lenient().when(merchantStatsTotalAmountService.findMonthlyTotalAmountMerchant(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getMonthlyTotalAmount(2024).await().indefinitely().status()).isEqualTo("success");
    }
}

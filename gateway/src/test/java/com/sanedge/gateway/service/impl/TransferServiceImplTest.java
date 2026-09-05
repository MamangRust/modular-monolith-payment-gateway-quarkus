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

import com.sanedge.gateway.dto.TransferDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock private TelemetryHelper telemetryHelper;
    @Mock private pb.transfer.MutinyTransferQueryServiceGrpc.MutinyTransferQueryServiceStub transferQueryService;
    @Mock private pb.transfer.MutinyTransferCommandServiceGrpc.MutinyTransferCommandServiceStub transferCommandService;
    @Mock private pb.transfer.stats.MutinyTransferStatsAmountServiceGrpc.MutinyTransferStatsAmountServiceStub transferStatsAmountService;
    @Mock private pb.transfer.stats.MutinyTransferStatsStatusServiceGrpc.MutinyTransferStatsStatusServiceStub transferStatsStatusService;

    private TransferServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> { Supplier<Uni<?>> s = inv.getArgument(1); return s.get(); });
        service = new TransferServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("transferQueryService", transferQueryService);
        inject("transferCommandService", transferCommandService);
        inject("transferStatsAmountService", transferStatsAmountService);
        inject("transferStatsStatusService", transferStatsStatusService);
    }

    private void inject(String n, Object v) throws Exception {
        Field f = TransferServiceImpl.class.getDeclaredField(n);
        f.setAccessible(true); f.set(service, v);
    }

    @Test void listTransfers_PropagatesResponse() {
        pb.transfer.TransferQuery.ApiResponsePaginationTransfer proto = pb.transfer.TransferQuery.ApiResponsePaginationTransfer.newBuilder().setStatus("success").build();
        lenient().when(transferQueryService.findAllTransfer(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.listTransfers(1, 10, "").await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getTransfer_PropagatesResponse() {
        pb.transfer.Transfer.ApiResponseTransfer proto = pb.transfer.Transfer.ApiResponseTransfer.newBuilder().setStatus("success").build();
        lenient().when(transferQueryService.findByIdTransfer(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getTransfer(1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void createTransfer_PropagatesResponse() {
        TransferDto.CreateRequest req = new TransferDto.CreateRequest("111122223333", "444455556666", 100000);
        pb.transfer.Transfer.ApiResponseTransfer proto = pb.transfer.Transfer.ApiResponseTransfer.newBuilder().setStatus("success").setMessage("created").build();
        lenient().when(transferCommandService.createTransfer(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.createTransfer(req).await().indefinitely().message()).isEqualTo("created");
    }

    @Test void findMonthlyAmounts_PropagatesResponse() {
        pb.transfer.stats.TransferStatsAmount.ApiResponseTransferMonthAmount proto = pb.transfer.stats.TransferStatsAmount.ApiResponseTransferMonthAmount.newBuilder().setStatus("success").build();
        lenient().when(transferStatsAmountService.findMonthlyTransferAmounts(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyAmounts(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void findMonthlyStatusSuccess_PropagatesResponse() {
        pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusSuccess proto = pb.transfer.stats.TransferStatsStatus.ApiResponseTransferMonthStatusSuccess.newBuilder().setStatus("success").build();
        lenient().when(transferStatsStatusService.findMonthlyTransferStatusSuccess(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyStatusSuccess(2024, 6).await().indefinitely().status()).isEqualTo("success");
    }
}

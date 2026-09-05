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

import com.sanedge.gateway.dto.WithdrawDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class WithdrawServiceImplTest {

    @Mock private TelemetryHelper telemetryHelper;
    @Mock private pb.withdraw.MutinyWithdrawQueryServiceGrpc.MutinyWithdrawQueryServiceStub withdrawQueryService;
    @Mock private pb.withdraw.MutinyWithdrawCommandServiceGrpc.MutinyWithdrawCommandServiceStub withdrawCommandService;
    @Mock private pb.withdraw.stats.MutinyWithdrawStatsAmountServiceGrpc.MutinyWithdrawStatsAmountServiceStub withdrawStatsAmountService;
    @Mock private pb.withdraw.stats.MutinyWithdrawStatsStatusServiceGrpc.MutinyWithdrawStatsStatusServiceStub withdrawStatsStatusService;

    private WithdrawServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(inv -> { Supplier<Uni<?>> s = inv.getArgument(1); return s.get(); });
        service = new WithdrawServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("withdrawQueryService", withdrawQueryService);
        inject("withdrawCommandService", withdrawCommandService);
        inject("withdrawStatsAmountService", withdrawStatsAmountService);
        inject("withdrawStatsStatusService", withdrawStatsStatusService);
    }

    private void inject(String n, Object v) throws Exception {
        Field f = WithdrawServiceImpl.class.getDeclaredField(n);
        f.setAccessible(true); f.set(service, v);
    }

    @Test void listWithdraws_PropagatesResponse() {
        pb.withdraw.WithdrawQuery.ApiResponsePaginationWithdraw proto = pb.withdraw.WithdrawQuery.ApiResponsePaginationWithdraw.newBuilder().setStatus("success").build();
        lenient().when(withdrawQueryService.findAllWithdraw(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.listWithdraws(1, 10, "").await().indefinitely().status()).isEqualTo("success");
    }

    @Test void getWithdraw_PropagatesResponse() {
        pb.withdraw.Withdraw.ApiResponseWithdraw proto = pb.withdraw.Withdraw.ApiResponseWithdraw.newBuilder().setStatus("success").build();
        lenient().when(withdrawQueryService.findByIdWithdraw(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.getWithdraw(1).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void createWithdraw_PropagatesResponse() {
        WithdrawDto.CreateRequest req = new WithdrawDto.CreateRequest("1234567890", 50000);
        pb.withdraw.Withdraw.ApiResponseWithdraw proto = pb.withdraw.Withdraw.ApiResponseWithdraw.newBuilder().setStatus("success").setMessage("created").build();
        lenient().when(withdrawCommandService.createWithdraw(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.createWithdraw(req).await().indefinitely().message()).isEqualTo("created");
    }

    @Test void findMonthlyAmounts_PropagatesResponse() {
        pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawMonthAmount proto = pb.withdraw.stats.WithdrawStatsAmount.ApiResponseWithdrawMonthAmount.newBuilder().setStatus("success").build();
        lenient().when(withdrawStatsAmountService.findMonthlyWithdraws(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyAmounts(2024).await().indefinitely().status()).isEqualTo("success");
    }

    @Test void findMonthlyStatusSuccess_PropagatesResponse() {
        pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess proto = pb.withdraw.stats.WithdrawStatsStatus.ApiResponseWithdrawMonthStatusSuccess.newBuilder().setStatus("success").build();
        lenient().when(withdrawStatsStatusService.findMonthlyWithdrawStatusSuccess(any())).thenAnswer(i -> Uni.createFrom().item(proto));
        assertThat(service.findMonthlyStatusSuccess(2024, 6).await().indefinitely().status()).isEqualTo("success");
    }
}

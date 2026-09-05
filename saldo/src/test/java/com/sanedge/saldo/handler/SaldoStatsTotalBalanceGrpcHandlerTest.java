package com.sanedge.saldo.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoMonthTotalBalanceResponse;
import com.sanedge.saldo.domain.response.stats.total_balance.SaldoYearTotalBalanceResponse;
import com.sanedge.saldo.service.stats.SaldoTotalAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.saldo.stats.SaldoStatsTotal;

@ExtendWith(MockitoExtension.class)
class SaldoStatsTotalBalanceGrpcHandlerTest {

        @Mock
        private SaldoTotalAmountService saldoTotalAmountService;

        private SaldoStatsTotalBalanceGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new SaldoStatsTotalBalanceGrpcHandler();
                handler.saldoTotalAmountService = saldoTotalAmountService;
        }

        @Test
        @DisplayName("findMonthlyTotalSaldoBalance - success")
        void findMonthly_Success() {
                pb.saldo.Saldo.FindMonthlySaldoTotalBalance request = pb.saldo.Saldo.FindMonthlySaldoTotalBalance
                                .newBuilder()
                                .setYear(2024).setMonth(6).build();

                List<SaldoMonthTotalBalanceResponse> data = List
                                .of(new SaldoMonthTotalBalanceResponse("Jun", "2024", 500000L));
                ApiResponse<List<SaldoMonthTotalBalanceResponse>> apiResp = ApiResponse.success("Success", data);
                when(saldoTotalAmountService.findMonthTotalBalance(any())).thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsTotal.ApiResponseMonthTotalSaldo response = handler.findMonthlyTotalSaldoBalance(request)
                                .await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(500000);
        }

        @Test
        @DisplayName("findMonthlyTotalSaldoBalance - error")
        void findMonthly_Error() {
                pb.saldo.Saldo.FindMonthlySaldoTotalBalance request = pb.saldo.Saldo.FindMonthlySaldoTotalBalance
                                .newBuilder().build();
                when(saldoTotalAmountService.findMonthTotalBalance(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTotalSaldoBalance(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearTotalSaldoBalance - success")
        void findYear_Success() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().setYear(2024)
                                .build();

                List<SaldoYearTotalBalanceResponse> data = List.of(new SaldoYearTotalBalanceResponse("2024", 1000000L));
                ApiResponse<List<SaldoYearTotalBalanceResponse>> apiResp = ApiResponse.success("Success", data);
                when(saldoTotalAmountService.findYearTotalBalance(anyLong()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsTotal.ApiResponseYearTotalSaldo response = handler.findYearTotalSaldoBalance(request).await()
                                .indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(1000000);
        }

        @Test
        @DisplayName("findYearTotalSaldoBalance - error")
        void findYear_Error() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().build();
                when(saldoTotalAmountService.findYearTotalBalance(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearTotalSaldoBalance(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // edge cases
        @Test
        @DisplayName("findMonthly - null data")
        void findMonthly_NullData() {
                pb.saldo.Saldo.FindMonthlySaldoTotalBalance request = pb.saldo.Saldo.FindMonthlySaldoTotalBalance
                                .newBuilder()
                                .setYear(2024).setMonth(6).build();
                ApiResponse<List<SaldoMonthTotalBalanceResponse>> apiResp = ApiResponse.success("Success", null);
                when(saldoTotalAmountService.findMonthTotalBalance(any())).thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsTotal.ApiResponseMonthTotalSaldo response = handler.findMonthlyTotalSaldoBalance(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isZero();
        }
}
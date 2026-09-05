package com.sanedge.saldo.handler;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.sanedge.saldo.domain.response.stats.balance.SaldoMonthBalanceResponse;
import com.sanedge.saldo.domain.response.stats.balance.SaldoYearBalanceResponse;
import com.sanedge.saldo.service.stats.SaldoBalanceService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.saldo.stats.SaldoStatsBalance;

@ExtendWith(MockitoExtension.class)
class SaldoStatsBalanceGrpcHandlerTest {

        @Mock
        private SaldoBalanceService saldoBalanceService;

        private SaldoStatsBalanceGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new SaldoStatsBalanceGrpcHandler();
                handler.saldoBalanceService = saldoBalanceService;
        }

        @Test
        @DisplayName("findMonthlySaldoBalances - success")
        void findMonthly_Success() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().setYear(2024)
                                .build();

                List<SaldoMonthBalanceResponse> data = List.of(new SaldoMonthBalanceResponse("Jan", 100000L));
                ApiResponse<List<SaldoMonthBalanceResponse>> apiResp = ApiResponse.success("Success", data);
                when(saldoBalanceService.getMonthBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsBalance.ApiResponseMonthSaldoBalances response = handler.findMonthlySaldoBalances(request)
                                .await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(100000);
        }

        @Test
        @DisplayName("findMonthlySaldoBalances - error")
        void findMonthly_Error() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().setYear(2024)
                                .build();
                when(saldoBalanceService.getMonthBalance(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlySaldoBalances(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlySaldoBalances - success")
        void findYearly_Success() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().setYear(2024)
                                .build();

                List<SaldoYearBalanceResponse> data = List.of(new SaldoYearBalanceResponse("2024", 500000L));
                ApiResponse<List<SaldoYearBalanceResponse>> apiResp = ApiResponse.success("Success", data);
                when(saldoBalanceService.getYearBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsBalance.ApiResponseYearSaldoBalances response = handler.findYearlySaldoBalances(request)
                                .await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalBalance()).isEqualTo(500000);
        }

        @Test
        @DisplayName("findYearlySaldoBalances - error")
        void findYearly_Error() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().build();
                when(saldoBalanceService.getYearBalance(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlySaldoBalances(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // edge cases
        @Test
        @DisplayName("findMonthly - null data")
        void findMonthly_NullData() {
                pb.saldo.Saldo.FindYearlySaldo request = pb.saldo.Saldo.FindYearlySaldo.newBuilder().setYear(2024)
                                .build();
                ApiResponse<List<SaldoMonthBalanceResponse>> apiResp = ApiResponse.success("Success", null);
                when(saldoBalanceService.getMonthBalance(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                SaldoStatsBalance.ApiResponseMonthSaldoBalances response = handler.findMonthlySaldoBalances(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isZero();
        }
}
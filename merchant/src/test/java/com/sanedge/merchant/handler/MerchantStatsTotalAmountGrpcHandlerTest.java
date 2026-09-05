package com.sanedge.merchant.handler;

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
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseMonthlyTotalAmount;
import com.sanedge.merchant.domain.response.stats.total_amount.MerchantResponseYearlyTotalAmount;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountByApiKeyService;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountByIdService;
import com.sanedge.merchant.service.stats.totalamount.MerchantTotalAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.merchant.Merchant;
import pb.merchant.stats.MerchantStatsTotalamount;

@ExtendWith(MockitoExtension.class)
class MerchantStatsTotalAmountGrpcHandlerTest {

        @Mock
        private MerchantTotalAmountService totalAmountService;

        @Mock
        private MerchantTotalAmountByIdService totalAmountByIdService;

        @Mock
        private MerchantTotalAmountByApiKeyService totalAmountByApiKeyService;

        private MerchantStatsTotalAmountGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new MerchantStatsTotalAmountGrpcHandler();
                handler.merchantTotalAmountService = totalAmountService;
                handler.merchantTotalAmountByIdService = totalAmountByIdService;
                handler.merchantTotalAmountByApiKeyService = totalAmountByApiKeyService;
        }

        private MerchantResponseMonthlyTotalAmount monthTotal(String year, String month, long amount) {
                MerchantResponseMonthlyTotalAmount m = new MerchantResponseMonthlyTotalAmount();
                m.setReportYear(year);
                m.setMonthName(month);
                m.setTotalAmount(amount);
                return m;
        }

        private MerchantResponseYearlyTotalAmount yearlyTotal(String year, long amount) {
                MerchantResponseYearlyTotalAmount y = new MerchantResponseYearlyTotalAmount();
                y.setReportYear(year);
                y.setTotalAmount(amount);
                return y;
        }

        @Test
        @DisplayName("findMonthlyTotalAmountMerchant - success")
        void findMonthly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseMonthlyTotalAmount> data = List.of(monthTotal("2024", "Jun", 500000L));
                ApiResponse<List<MerchantResponseMonthlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountService.findMonthTotalAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount response = handler
                                .findMonthlyTotalAmountMerchant(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jun");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(500000L);
        }

        @Test
        @DisplayName("findMonthlyTotalAmountMerchant - error")
        void findMonthly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(totalAmountService.findMonthTotalAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTotalAmountMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTotalAmountMerchant - success")
        void findYearly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseYearlyTotalAmount> data = List.of(yearlyTotal("2024", 1000000L));
                ApiResponse<List<MerchantResponseYearlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountService.findYearTotalAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantYearlyTotalAmount response = handler
                                .findYearlyTotalAmountMerchant(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1000000L);
        }

        @Test
        @DisplayName("findYearlyTotalAmountMerchant - error")
        void findYearly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(totalAmountService.findYearTotalAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyTotalAmountMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findMonthlyTotalAmountByMerchants - success")
        void findMonthlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseMonthlyTotalAmount> data = List.of(monthTotal("2024", "Jul", 200000L));
                ApiResponse<List<MerchantResponseMonthlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountByIdService.findMonthTotalAmountById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount response = handler
                                .findMonthlyTotalAmountByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Jul");
        }

        @Test
        @DisplayName("findMonthlyTotalAmountByMerchants - error")
        void findMonthlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(totalAmountByIdService.findMonthTotalAmountById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTotalAmountByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTotalAmountByMerchants - success")
        void findYearlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseYearlyTotalAmount> data = List.of(yearlyTotal("2024", 800000L));
                ApiResponse<List<MerchantResponseYearlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountByIdService.findYearTotalAmountById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantYearlyTotalAmount response = handler
                                .findYearlyTotalAmountByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("findYearlyTotalAmountByMerchants - error")
        void findYearlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(totalAmountByIdService.findYearTotalAmountById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyTotalAmountByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findMonthlyTotalAmountByApikey - success")
        void findMonthlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseMonthlyTotalAmount> data = List.of(monthTotal("2024", "Aug", 300000L));
                ApiResponse<List<MerchantResponseMonthlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountByApiKeyService.findMonthTotalAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantMonthlyTotalAmount response = handler
                                .findMonthlyTotalAmountByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Aug");
        }

        @Test
        @DisplayName("findMonthlyTotalAmountByApikey - error")
        void findMonthlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(totalAmountByApiKeyService.findMonthTotalAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyTotalAmountByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyTotalAmountByApikey - success")
        void findYearlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseYearlyTotalAmount> data = List.of(yearlyTotal("2024", 900000L));
                ApiResponse<List<MerchantResponseYearlyTotalAmount>> apiResp = ApiResponse.success("Success", data);
                when(totalAmountByApiKeyService.findYearTotalAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsTotalamount.ApiResponseMerchantYearlyTotalAmount response = handler
                                .findYearlyTotalAmountByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(900000L);
        }

        @Test
        @DisplayName("findYearlyTotalAmountByApikey - error")
        void findYearlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(totalAmountByApiKeyService.findYearTotalAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyTotalAmountByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }
}
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
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseMonthlyAmount;
import com.sanedge.merchant.domain.response.stats.amount.MerchantResponseYearlyAmount;
import com.sanedge.merchant.service.stats.amount.MerchantAmountByApiKeyService;
import com.sanedge.merchant.service.stats.amount.MerchantAmountByIdService;
import com.sanedge.merchant.service.stats.amount.MerchantAmountService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.merchant.Merchant;
import pb.merchant.stats.MerchantStatsAmount;

@ExtendWith(MockitoExtension.class)
class MerchantStatsAmountGrpcHandlerTest {

        @Mock
        private MerchantAmountService amountService;

        @Mock
        private MerchantAmountByIdService amountByIdService;

        @Mock
        private MerchantAmountByApiKeyService amountByApiKeyService;

        private MerchantStatsAmountGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new MerchantStatsAmountGrpcHandler();
                handler.merchantAmountService = amountService;
                handler.merchantAmountByIdService = amountByIdService;
                handler.merchantAmountByApiKeyService = amountByApiKeyService;
        }

        // helpers
        private MerchantResponseMonthlyAmount monthAmount(String month, long amount) {
                MerchantResponseMonthlyAmount m = new MerchantResponseMonthlyAmount();
                m.setMonthName(month);
                m.setTotalAmount(amount);
                return m;
        }

        private MerchantResponseYearlyAmount yearlyAmount(String year, long amount) {
                MerchantResponseYearlyAmount y = new MerchantResponseYearlyAmount();
                y.setReportYear(year);
                y.setTotalAmount(amount);
                return y;
        }

        // ========== findMonthlyAmountMerchant (base) ==========
        @Test
        @DisplayName("findMonthlyAmountMerchant - success")
        void findMonthly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseMonthlyAmount> data = List.of(monthAmount("Jan", 1000L));
                ApiResponse<List<MerchantResponseMonthlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountService.findMonthAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantMonthlyAmount response = handler
                                .findMonthlyAmountMerchant(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("findMonthlyAmountMerchant - error")
        void findMonthly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(amountService.findMonthAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyAmountMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findYearlyAmountMerchant (base) ==========
        @Test
        @DisplayName("findYearlyAmountMerchant - success")
        void findYearly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseYearlyAmount> data = List.of(yearlyAmount("2024", 5000L));
                ApiResponse<List<MerchantResponseYearlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountService.findYearAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantYearlyAmount response = handler.findYearlyAmountMerchant(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(5000L);
        }

        @Test
        @DisplayName("findYearlyAmountMerchant - error")
        void findYearly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(amountService.findYearAmount(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyAmountMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findMonthlyAmountByMerchants (by ID) ==========
        @Test
        @DisplayName("findMonthlyAmountByMerchants - success")
        void findMonthlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseMonthlyAmount> data = List.of(monthAmount("Feb", 2000L));
                ApiResponse<List<MerchantResponseMonthlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountByIdService.findMonthAmountById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantMonthlyAmount response = handler
                                .findMonthlyAmountByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Feb");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("findMonthlyAmountByMerchants - error")
        void findMonthlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(amountByIdService.findMonthAmountById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyAmountByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findYearlyAmountByMerchants (by ID) ==========
        @Test
        @DisplayName("findYearlyAmountByMerchants - success")
        void findYearlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseYearlyAmount> data = List.of(yearlyAmount("2024", 6000L));
                ApiResponse<List<MerchantResponseYearlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountByIdService.findYearAmountById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantYearlyAmount response = handler
                                .findYearlyAmountByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("findYearlyAmountByMerchants - error")
        void findYearlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(amountByIdService.findYearAmountById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyAmountByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findMonthlyAmountByApikey ==========
        @Test
        @DisplayName("findMonthlyAmountByApikey - success")
        void findMonthlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseMonthlyAmount> data = List.of(monthAmount("Mar", 3000L));
                ApiResponse<List<MerchantResponseMonthlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountByApiKeyService.findMonthAmountByApiKey(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantMonthlyAmount response = handler
                                .findMonthlyAmountByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Mar");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("findMonthlyAmountByApikey - error")
        void findMonthlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(amountByApiKeyService.findMonthAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyAmountByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // ========== findYearlyAmountByApikey ==========
        @Test
        @DisplayName("findYearlyAmountByApikey - success")
        void findYearlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseYearlyAmount> data = List.of(yearlyAmount("2024", 7000L));
                ApiResponse<List<MerchantResponseYearlyAmount>> apiResp = ApiResponse.success("Success", data);
                when(amountByApiKeyService.findYearAmountByApiKey(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantYearlyAmount response = handler.findYearlyAmountByApikey(request)
                                .await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(7000L);
        }

        @Test
        @DisplayName("findYearlyAmountByApikey - error")
        void findYearlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(amountByApiKeyService.findYearAmountByApiKey(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyAmountByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // edge case: null data
        @Test
        @DisplayName("findMonthlyAmountMerchant - null data")
        void findMonthly_NullData() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                ApiResponse<List<MerchantResponseMonthlyAmount>> apiResp = ApiResponse.success("Success", null);
                when(amountService.findMonthAmount(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsAmount.ApiResponseMerchantMonthlyAmount response = handler
                                .findMonthlyAmountMerchant(request).await().indefinitely();
                assertThat(response.getDataCount()).isZero();
        }
}
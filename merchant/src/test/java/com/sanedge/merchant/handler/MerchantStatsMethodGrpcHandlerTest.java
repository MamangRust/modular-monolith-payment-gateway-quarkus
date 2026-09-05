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
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseMonthlyPaymentMethod;
import com.sanedge.merchant.domain.response.stats.method.MerchantResponseYearlyPaymentMethod;
import com.sanedge.merchant.service.stats.method.MerchantMethodByApiKeyService;
import com.sanedge.merchant.service.stats.method.MerchantMethodByIdService;
import com.sanedge.merchant.service.stats.method.MerchantMethodService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.merchant.Merchant;
import pb.merchant.stats.MerchantStatsMethod;

@ExtendWith(MockitoExtension.class)
class MerchantStatsMethodGrpcHandlerTest {

        @Mock
        private MerchantMethodService methodService;

        @Mock
        private MerchantMethodByIdService methodByIdService;

        @Mock
        private MerchantMethodByApiKeyService methodByApiKeyService;

        private MerchantStatsMethodGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new MerchantStatsMethodGrpcHandler();
                handler.merchantMethodService = methodService;
                handler.merchantMethodByIdService = methodByIdService;
                handler.merchantMethodByApiKeyService = methodByApiKeyService;
        }

        private MerchantResponseMonthlyPaymentMethod monthMethod(String month, String method, long amount) {
                MerchantResponseMonthlyPaymentMethod m = new MerchantResponseMonthlyPaymentMethod();
                m.setMonthName(month);
                m.setPaymentMethod(method);
                m.setTotalAmount(amount);
                return m;
        }

        private MerchantResponseYearlyPaymentMethod yearlyMethod(String year, String method, long amount) {
                MerchantResponseYearlyPaymentMethod y = new MerchantResponseYearlyPaymentMethod();
                y.setReportYear(year);
                y.setPaymentMethod(method);
                y.setTotalAmount(amount);
                return y;
        }

        @Test
        @DisplayName("findMonthlyPaymentMethodsMerchant - success")
        void findMonthly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseMonthlyPaymentMethod> data = List.of(monthMethod("Jan", "CREDIT", 1000L));
                ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodService.findMonthMethod(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod response = handler
                                .findMonthlyPaymentMethodsMerchant(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getMonth()).isEqualTo("Jan");
                assertThat(response.getData(0).getPaymentMethod()).isEqualTo("CREDIT");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("findMonthlyPaymentMethodsMerchant - error")
        void findMonthly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(methodService.findMonthMethod(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyPaymentMethodsMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyPaymentMethodMerchant - success")
        void findYearly_Success() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().setYear(2024).build();
                List<MerchantResponseYearlyPaymentMethod> data = List.of(yearlyMethod("2024", "DEBIT", 2000L));
                ApiResponse<List<MerchantResponseYearlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodService.findYearMethod(anyLong())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantYearlyPaymentMethod response = handler
                                .findYearlyPaymentMethodMerchant(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
                assertThat(response.getData(0).getPaymentMethod()).isEqualTo("DEBIT");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(2000L);
        }

        @Test
        @DisplayName("findYearlyPaymentMethodMerchant - error")
        void findYearly_Error() {
                Merchant.FindYearMerchant request = Merchant.FindYearMerchant.newBuilder().build();
                when(methodService.findYearMethod(anyLong()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyPaymentMethodMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // -- by ID --
        @Test
        @DisplayName("findMonthlyPaymentMethodByMerchants - success")
        void findMonthlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseMonthlyPaymentMethod> data = List.of(monthMethod("Feb", "QRIS", 3000L));
                ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodByIdService.findMonthMethodById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod response = handler
                                .findMonthlyPaymentMethodByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMonth()).isEqualTo("Feb");
                assertThat(response.getData(0).getPaymentMethod()).isEqualTo("QRIS");
        }

        @Test
        @DisplayName("findMonthlyPaymentMethodByMerchants - error")
        void findMonthlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(methodByIdService.findMonthMethodById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyPaymentMethodByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyPaymentMethodByMerchants - success")
        void findYearlyById_Success() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder()
                                .setMerchantId(1).setYear(2024).build();
                List<MerchantResponseYearlyPaymentMethod> data = List.of(yearlyMethod("2024", "QRIS", 4000L));
                ApiResponse<List<MerchantResponseYearlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodByIdService.findYearMethodById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantYearlyPaymentMethod response = handler
                                .findYearlyPaymentMethodByMerchants(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("findYearlyPaymentMethodByMerchants - error")
        void findYearlyById_Error() {
                Merchant.FindYearMerchantById request = Merchant.FindYearMerchantById.newBuilder().build();
                when(methodByIdService.findYearMethodById(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyPaymentMethodByMerchants(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        // -- by API key --
        @Test
        @DisplayName("findMonthlyPaymentMethodByApikey - success")
        void findMonthlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseMonthlyPaymentMethod> data = List.of(monthMethod("Mar", "CASH", 5000L));
                ApiResponse<List<MerchantResponseMonthlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodByApiKeyService.findMonthMethod(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantMonthlyPaymentMethod response = handler
                                .findMonthlyPaymentMethodByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getPaymentMethod()).isEqualTo("CASH");
        }

        @Test
        @DisplayName("findMonthlyPaymentMethodByApikey - error")
        void findMonthlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(methodByApiKeyService.findMonthMethod(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findMonthlyPaymentMethodByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findYearlyPaymentMethodByApikey - success")
        void findYearlyByApiKey_Success() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder()
                                .setApiKey("key-123").setYear(2024).build();
                List<MerchantResponseYearlyPaymentMethod> data = List.of(yearlyMethod("2024", "CASH", 6000L));
                ApiResponse<List<MerchantResponseYearlyPaymentMethod>> apiResp = ApiResponse.success("Success", data);
                when(methodByApiKeyService.findYearMethod(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantStatsMethod.ApiResponseMerchantYearlyPaymentMethod response = handler
                                .findYearlyPaymentMethodByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getTotalAmount()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("findYearlyPaymentMethodByApikey - error")
        void findYearlyByApiKey_Error() {
                Merchant.FindYearMerchantByApikey request = Merchant.FindYearMerchantByApikey.newBuilder().build();
                when(methodByApiKeyService.findYearMethod(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findYearlyPaymentMethodByApikey(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }
}
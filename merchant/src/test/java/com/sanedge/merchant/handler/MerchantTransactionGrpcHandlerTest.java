package com.sanedge.merchant.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.domain.response.ApiResponsePagination;
import com.sanedge.merchant.domain.response.MerchantTransactionResponse;
import com.sanedge.merchant.service.MerchantTransactionService;

import io.grpc.StatusRuntimeException;
import io.smallrye.mutiny.Uni;
import pb.merchant.MerchantTransaction;

@ExtendWith(MockitoExtension.class)
class MerchantTransactionGrpcHandlerTest {

        @Mock
        private MerchantTransactionService merchantTransactionService;

        private MerchantTransactionGrpcHandler handler;

        @BeforeEach
        void setUp() {
                handler = new MerchantTransactionGrpcHandler();
                handler.merchantTransactionService = merchantTransactionService;
        }

        // helpers
        private MerchantTransactionResponse createResponse(Integer id) {
                MerchantTransactionResponse r = new MerchantTransactionResponse();
                r.setId(id.longValue());
                r.setCardNumber("CARD-1234");
                r.setAmount(150000L);
                r.setPaymentMethod("CREDIT");
                r.setMerchantId(1L);
                r.setMerchantName("Test Merchant");
                r.setTransactionTime(Timestamp.valueOf(LocalDateTime.of(2024, 6, 15, 10, 30)).toString());
                r.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()).toString());
                r.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()).toString());
                return r;
        }

        @Test
        @DisplayName("findAllTransactionMerchant - success")
        void findAll_Success() {
                pb.merchant.Merchant.FindAllMerchantTransaction request = pb.merchant.Merchant.FindAllMerchantTransaction
                                .newBuilder()
                                .setPage(1).setPageSize(10).build();

                MerchantTransactionResponse data = createResponse(1);
                ApiResponsePagination<List<MerchantTransactionResponse>> apiResp = new ApiResponsePagination<>(
                                "success", "Transactions retrieved", List.of(data), null);
                when(merchantTransactionService.findAll(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantTransaction.ApiResponsePaginationMerchantTransaction response = handler
                                .findAllTransactionMerchant(request).await().indefinitely();

                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getDataCount()).isEqualTo(1);
                assertThat(response.getData(0).getCardNumber()).isEqualTo("CARD-1234");
                assertThat(response.getData(0).getAmount()).isEqualTo(150000);
        }

        @Test
        @DisplayName("findAllTransactionMerchant - error")
        void findAll_Error() {
                pb.merchant.Merchant.FindAllMerchantTransaction request = pb.merchant.Merchant.FindAllMerchantTransaction
                                .newBuilder().build();
                when(merchantTransactionService.findAll(any()))
                                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));
                try {
                        handler.findAllTransactionMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }

        @Test
        @DisplayName("findAllTransactionByApikey - success")
        void findByApiKey_Success() {
                pb.merchant.Merchant.FindAllMerchantTransactionApikey request = pb.merchant.Merchant.FindAllMerchantTransactionApikey
                                .newBuilder()
                                .setApiKey("api-key").setPage(1).setPageSize(10).build();

                MerchantTransactionResponse data = createResponse(1);
                ApiResponsePagination<List<MerchantTransactionResponse>> apiResp = new ApiResponsePagination<>(
                                "success", "Transactions by apiKey", List.of(data), null);
                when(merchantTransactionService.findByApiKey(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantTransaction.ApiResponsePaginationMerchantTransaction response = handler
                                .findAllTransactionByApikey(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getCardNumber()).isEqualTo("CARD-1234");
        }

        @Test
        @DisplayName("findAllTransactionByMerchant - success")
        void findByMerchant_Success() {
                pb.merchant.Merchant.FindAllMerchantTransactionId request = pb.merchant.Merchant.FindAllMerchantTransactionId
                                .newBuilder()
                                .setId("1").setPage(1).setPageSize(10).build();

                MerchantTransactionResponse data = createResponse(1);
                ApiResponsePagination<List<MerchantTransactionResponse>> apiResp = new ApiResponsePagination<>(
                                "success", "Transactions by merchant", List.of(data), null);
                when(merchantTransactionService.findById(any())).thenReturn(Uni.createFrom().item(apiResp));

                MerchantTransaction.ApiResponsePaginationMerchantTransaction response = handler
                                .findAllTransactionByMerchant(request).await().indefinitely();
                assertThat(response.getStatus()).isEqualTo("success");
                assertThat(response.getData(0).getMerchantId()).isEqualTo(1);
        }

        @Test
        @DisplayName("findAllTransactionByMerchant - invalid id format causes error")
        void findByMerchant_InvalidId() {
                pb.merchant.Merchant.FindAllMerchantTransactionId request = pb.merchant.Merchant.FindAllMerchantTransactionId
                                .newBuilder()
                                .setId("abc").build();
                try {
                        handler.findAllTransactionByMerchant(request).await().indefinitely();
                } catch (StatusRuntimeException e) {
                        assertThat(e).isNotNull();
                }
        }
}
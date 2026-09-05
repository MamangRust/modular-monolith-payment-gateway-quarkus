package com.sanedge.gateway.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.MerchantDto;
import com.sanedge.gateway.service.MerchantService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class MerchantResourceTest {

    @Mock private MerchantService merchantService;
    private MerchantResource merchantResource;

    @BeforeEach
    void setUp() throws Exception {
        merchantResource = new MerchantResource();
        Field f = MerchantResource.class.getDeclaredField("merchantService");
        f.setAccessible(true); f.set(merchantResource, merchantService);
    }

    @Test void listMerchants_Success() {
        MerchantDto.ApiResponsePaginationMerchant dto = new MerchantDto.ApiResponsePaginationMerchant("success", "ok", List.of(), null);
        lenient().when(merchantService.listMerchants(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantResource.listMerchants(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getMerchant_Success() {
        MerchantDto.ApiResponseMerchant dto = new MerchantDto.ApiResponseMerchant("success", "ok", null);
        lenient().when(merchantService.getMerchant(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantResource.getMerchant(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createMerchant_Success_Returns201() {
        MerchantDto.ApiResponseMerchant dto = new MerchantDto.ApiResponseMerchant("success", "ok", null);
        lenient().when(merchantService.createMerchant(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantResource.createMerchant(new MerchantDto.CreateRequest("Test", 1)).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findAllTransactions_Success() {
        MerchantDto.ApiResponsePaginationMerchantTransaction dto = new MerchantDto.ApiResponsePaginationMerchantTransaction("success", "ok", List.of(), null);
        lenient().when(merchantService.findAllTransactions(anyInt(), anyInt(), anyString(), anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantResource.findAllTransactions(1, 10, "", 1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getMonthlyAmount_Success() {
        MerchantDto.ApiResponseMerchantMonthlyAmount dto = new MerchantDto.ApiResponseMerchantMonthlyAmount("success", "ok", null);
        lenient().when(merchantService.getMonthlyAmount(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantResource.getMonthlyAmount(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

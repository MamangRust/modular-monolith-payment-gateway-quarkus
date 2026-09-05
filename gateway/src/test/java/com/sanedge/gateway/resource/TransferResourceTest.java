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

import com.sanedge.gateway.dto.TransferDto;
import com.sanedge.gateway.service.TransferService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class TransferResourceTest {

    @Mock private TransferService transferService;
    private TransferResource transferResource;

    @BeforeEach
    void setUp() throws Exception {
        transferResource = new TransferResource();
        Field f = TransferResource.class.getDeclaredField("transferService");
        f.setAccessible(true); f.set(transferResource, transferService);
    }

    @Test void listTransfers_Success() {
        TransferDto.ApiResponsePaginationTransfer dto = new TransferDto.ApiResponsePaginationTransfer("success", "ok", List.of(), null);
        lenient().when(transferService.listTransfers(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transferResource.listTransfers(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getTransfer_Success() {
        TransferDto.ApiResponseTransfer dto = new TransferDto.ApiResponseTransfer("success", "ok", null);
        lenient().when(transferService.getTransfer(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transferResource.getTransfer(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createTransfer_Success_Returns201() {
        TransferDto.ApiResponseTransfer dto = new TransferDto.ApiResponseTransfer("success", "ok", null);
        lenient().when(transferService.createTransfer(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transferResource.createTransfer(new TransferDto.CreateRequest("111", "222", 100000)).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findMonthlyAmounts_Success() {
        TransferDto.ApiResponseTransferMonthAmount dto = new TransferDto.ApiResponseTransferMonthAmount("success", "ok", null);
        lenient().when(transferService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(transferResource.findMonthlyAmounts(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

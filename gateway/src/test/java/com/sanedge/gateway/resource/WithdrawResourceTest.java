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

import com.sanedge.gateway.dto.WithdrawDto;
import com.sanedge.gateway.service.WithdrawService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class WithdrawResourceTest {

    @Mock private WithdrawService withdrawService;
    private WithdrawResource withdrawResource;

    @BeforeEach
    void setUp() throws Exception {
        withdrawResource = new WithdrawResource();
        Field f = WithdrawResource.class.getDeclaredField("withdrawService");
        f.setAccessible(true); f.set(withdrawResource, withdrawService);
    }

    @Test void listWithdraws_Success() {
        WithdrawDto.ApiResponsePaginationWithdraw dto = new WithdrawDto.ApiResponsePaginationWithdraw("success", "ok", List.of(), null);
        lenient().when(withdrawService.listWithdraws(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(withdrawResource.listWithdraws(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getWithdraw_Success() {
        WithdrawDto.ApiResponseWithdraw dto = new WithdrawDto.ApiResponseWithdraw("success", "ok", null);
        lenient().when(withdrawService.getWithdraw(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(withdrawResource.getWithdraw(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createWithdraw_Success_Returns201() {
        WithdrawDto.ApiResponseWithdraw dto = new WithdrawDto.ApiResponseWithdraw("success", "ok", null);
        lenient().when(withdrawService.createWithdraw(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(withdrawResource.createWithdraw(new WithdrawDto.CreateRequest("1234567890", 50000)).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findMonthlyAmounts_Success() {
        WithdrawDto.ApiResponseWithdrawMonthAmount dto = new WithdrawDto.ApiResponseWithdrawMonthAmount("success", "ok", null);
        lenient().when(withdrawService.findMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(withdrawResource.findMonthlyAmounts(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

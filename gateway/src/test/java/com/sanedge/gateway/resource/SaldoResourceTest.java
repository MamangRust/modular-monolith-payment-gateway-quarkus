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

import com.sanedge.gateway.dto.SaldoDto;
import com.sanedge.gateway.service.SaldoService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class SaldoResourceTest {

    @Mock private SaldoService saldoService;
    private SaldoResource saldoResource;

    @BeforeEach
    void setUp() throws Exception {
        saldoResource = new SaldoResource();
        Field f = SaldoResource.class.getDeclaredField("saldoService");
        f.setAccessible(true); f.set(saldoResource, saldoService);
    }

    @Test void listSaldos_Success() {
        SaldoDto.ApiResponsePaginationSaldo dto = new SaldoDto.ApiResponsePaginationSaldo("success", "ok", List.of(), null);
        lenient().when(saldoService.listSaldos(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(saldoResource.listSaldos(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getSaldo_Success() {
        SaldoDto.ApiResponseSaldo dto = new SaldoDto.ApiResponseSaldo("success", "ok", null);
        lenient().when(saldoService.getSaldo(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(saldoResource.getSaldo(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createSaldo_Success_Returns201() {
        SaldoDto.ApiResponseSaldo dto = new SaldoDto.ApiResponseSaldo("success", "ok", null);
        lenient().when(saldoService.createSaldo(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(saldoResource.createSaldo(new SaldoDto.CreateRequest("1234567890", 100000)).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findMonthlySaldoBalances_Success() {
        SaldoDto.ApiResponseMonthSaldoBalances dto = new SaldoDto.ApiResponseMonthSaldoBalances("success", "ok", null);
        lenient().when(saldoService.findMonthlySaldoBalances(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(saldoResource.findMonthlySaldoBalances(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

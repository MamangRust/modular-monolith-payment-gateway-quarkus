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

import com.sanedge.gateway.dto.TopupDto;
import com.sanedge.gateway.service.TopupService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class TopupResourceTest {

    @Mock private TopupService topupService;
    private TopupResource topupResource;

    @BeforeEach
    void setUp() throws Exception {
        topupResource = new TopupResource();
        Field f = TopupResource.class.getDeclaredField("topupService");
        f.setAccessible(true); f.set(topupResource, topupService);
    }

    @Test void listTopups_Success() {
        TopupDto.ApiResponsePaginationTopup dto = new TopupDto.ApiResponsePaginationTopup("success", "ok", List.of(), null);
        lenient().when(topupService.listTopups(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(topupResource.listTopups(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getTopup_Success() {
        TopupDto.ApiResponseTopup dto = new TopupDto.ApiResponseTopup("success", "ok", null);
        lenient().when(topupService.getTopup(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(topupResource.getTopup(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createTopup_Success_Returns201() {
        TopupDto.ApiResponseTopup dto = new TopupDto.ApiResponseTopup("success", "ok", null);
        lenient().when(topupService.createTopup(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(topupResource.createTopup(new TopupDto.CreateRequest("1234567890", 50000, "BANK_TRANSFER")).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void getMonthlyAmounts_Success() {
        TopupDto.ApiResponseTopupMonthAmount dto = new TopupDto.ApiResponseTopupMonthAmount("success", "ok", null);
        lenient().when(topupService.getMonthlyAmounts(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(topupResource.getMonthlyAmounts(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getMonthlyStatusSuccess_Success() {
        TopupDto.ApiResponseTopupMonthStatusSuccess dto = new TopupDto.ApiResponseTopupMonthStatusSuccess("success", "ok", null);
        lenient().when(topupService.getMonthlyStatusSuccess(anyInt(), anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(topupResource.getMonthlyStatusSuccess(2024, 6).await().indefinitely().getStatus()).isEqualTo(200);
    }
}

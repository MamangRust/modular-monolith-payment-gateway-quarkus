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

import com.sanedge.gateway.dto.CardDto;
import com.sanedge.gateway.service.CardService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class CardResourceTest {

    @Mock private CardService cardService;
    private CardResource cardResource;

    @BeforeEach
    void setUp() throws Exception {
        cardResource = new CardResource();
        Field f = CardResource.class.getDeclaredField("cardService");
        f.setAccessible(true); f.set(cardResource, cardService);
    }

    @Test void listCards_Success() {
        CardDto.ApiResponsePaginationCard dto = new CardDto.ApiResponsePaginationCard("success", "ok", List.of(), null);
        lenient().when(cardService.listCards(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(cardResource.listCards(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getCard_Success() {
        CardDto.ApiResponseCard dto = new CardDto.ApiResponseCard("success", "ok", null);
        lenient().when(cardService.getCard(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(cardResource.getCard(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createCard_Success_Returns201() {
        CardDto.ApiResponseCard dto = new CardDto.ApiResponseCard("success", "ok", null);
        lenient().when(cardService.createCard(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(cardResource.createCard(new CardDto.CreateRequest(1, "VISA", "2025-12-31", "123", "BCA")).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void findMonthlyBalance_Success() {
        CardDto.ApiResponseMonthlyBalance dto = new CardDto.ApiResponseMonthlyBalance("success", "ok", null);
        lenient().when(cardService.findMonthlyBalance(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(cardResource.findMonthlyBalance(2024).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void findCardDashboard_Success() {
        CardDto.ApiResponseDashboardCard dto = new CardDto.ApiResponseDashboardCard("success", "ok", null);
        lenient().when(cardService.findCardDashboard()).thenReturn(Uni.createFrom().item(dto));
        assertThat(cardResource.findCardDashboard().await().indefinitely().getStatus()).isEqualTo(200);
    }
}

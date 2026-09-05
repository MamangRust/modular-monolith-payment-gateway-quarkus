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

import com.sanedge.gateway.dto.MerchantDocumentDto;
import com.sanedge.gateway.service.MerchantDocumentService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class MerchantDocumentResourceTest {

    @Mock private MerchantDocumentService merchantDocumentService;
    private MerchantDocumentResource merchantDocumentResource;

    @BeforeEach
    void setUp() throws Exception {
        merchantDocumentResource = new MerchantDocumentResource();
        Field f = MerchantDocumentResource.class.getDeclaredField("merchantDocumentService");
        f.setAccessible(true); f.set(merchantDocumentResource, merchantDocumentService);
    }

    @Test void listMerchantDocuments_Success() {
        MerchantDocumentDto.ApiResponsePaginationMerchantDocument dto = new MerchantDocumentDto.ApiResponsePaginationMerchantDocument("success", "ok", List.of(), null);
        lenient().when(merchantDocumentService.listMerchantDocuments(anyInt(), anyInt(), anyString())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantDocumentResource.listMerchantDocuments(1, 10, "").await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void getMerchantDocument_Success() {
        MerchantDocumentDto.ApiResponseMerchantDocument dto = new MerchantDocumentDto.ApiResponseMerchantDocument("success", "ok", null);
        lenient().when(merchantDocumentService.getMerchantDocument(anyInt())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantDocumentResource.getMerchantDocument(1).await().indefinitely().getStatus()).isEqualTo(200);
    }

    @Test void createMerchantDocument_Success_Returns201() {
        MerchantDocumentDto.ApiResponseMerchantDocument dto = new MerchantDocumentDto.ApiResponseMerchantDocument("success", "ok", null);
        lenient().when(merchantDocumentService.createMerchantDocument(any())).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantDocumentResource.createMerchantDocument(new MerchantDocumentDto.CreateRequest(1, "PDF", "http://doc.url")).await().indefinitely().getStatus()).isEqualTo(201);
    }

    @Test void restoreAllMerchantDocuments_Success() {
        MerchantDocumentDto.SimpleResponse dto = new MerchantDocumentDto.SimpleResponse("success", "ok");
        lenient().when(merchantDocumentService.restoreAllMerchantDocuments()).thenReturn(Uni.createFrom().item(dto));
        assertThat(merchantDocumentResource.restoreAllMerchantDocuments().await().indefinitely().getStatus()).isEqualTo(200);
    }
}

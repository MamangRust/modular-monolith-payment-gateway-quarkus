package com.sanedge.gateway.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.gateway.dto.MerchantDocumentDto;
import com.sanedge.gateway.telemetry.TelemetryHelper;

import io.smallrye.mutiny.Uni;

@ExtendWith(MockitoExtension.class)
class MerchantDocumentServiceImplTest {

    @Mock
    private TelemetryHelper telemetryHelper;

    @Mock
    private pb.merchant_document.MutinyMerchantDocumentQueryServiceGrpc.MutinyMerchantDocumentQueryServiceStub merchantDocumentQueryService;

    @Mock
    private pb.merchant_document.MutinyMerchantDocumentCommandServiceGrpc.MutinyMerchantDocumentCommandServiceStub merchantDocumentCommandService;

    private MerchantDocumentServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(telemetryHelper.traceAndMetric(anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<Uni<?>> supplier = invocation.getArgument(1);
                    return supplier.get();
                });
        service = new MerchantDocumentServiceImpl();
        inject("telemetryHelper", telemetryHelper);
        inject("merchantDocumentQueryService", merchantDocumentQueryService);
        inject("merchantDocumentCommandService", merchantDocumentCommandService);
    }

    private void inject(String name, Object value) throws Exception {
        Field f = MerchantDocumentServiceImpl.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Test
    void listMerchantDocuments_PropagatesResponse() {
        pb.merchant_document.MerchantDocumentQuery.ApiResponsePaginationMerchantDocument proto =
                pb.merchant_document.MerchantDocumentQuery.ApiResponsePaginationMerchantDocument.newBuilder()
                        .setStatus("success").build();
        lenient().when(merchantDocumentQueryService.findAll(any()))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        MerchantDocumentDto.ApiResponsePaginationMerchantDocument result = service.listMerchantDocuments(1, 10, "").await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void getMerchantDocument_PropagatesResponse() {
        pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocument proto =
                pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocument.newBuilder()
                        .setStatus("success").setMessage("ok").build();
        lenient().when(merchantDocumentQueryService.findById(any()))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        MerchantDocumentDto.ApiResponseMerchantDocument result = service.getMerchantDocument(1).await().indefinitely();
        assertThat(result.status()).isEqualTo("success");
    }

    @Test
    void createMerchantDocument_PropagatesResponse() {
        MerchantDocumentDto.CreateRequest req = new MerchantDocumentDto.CreateRequest(1, "PDF", "http://doc.url");
        pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocument proto =
                pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocument.newBuilder()
                        .setStatus("success").setMessage("created").build();
        lenient().when(merchantDocumentCommandService.create(any()))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        MerchantDocumentDto.ApiResponseMerchantDocument result = service.createMerchantDocument(req).await().indefinitely();
        assertThat(result.message()).isEqualTo("created");
    }

    @Test
    void deleteMerchantDocument_PropagatesResponse() {
        pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocumentDeleteAt proto =
                pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocumentDeleteAt.newBuilder()
                        .setStatus("success").setMessage("trashed").build();
        lenient().when(merchantDocumentCommandService.trashed(any()))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        MerchantDocumentDto.ApiResponseMerchantDocumentDeleteAt result = service.deleteMerchantDocument(1).await().indefinitely();
        assertThat(result.message()).isEqualTo("trashed");
    }

    @Test
    void restoreMerchantDocument_PropagatesResponse() {
        pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocumentDeleteAt proto =
                pb.merchant_document.MerchantDocumentOuterClass.ApiResponseMerchantDocumentDeleteAt.newBuilder()
                        .setStatus("success").setMessage("restored").build();
        lenient().when(merchantDocumentCommandService.restore(any()))
                .thenAnswer(inv -> Uni.createFrom().item(proto));
        MerchantDocumentDto.ApiResponseMerchantDocumentDeleteAt result = service.restoreMerchantDocument(1).await().indefinitely();
        assertThat(result.message()).isEqualTo("restored");
    }

    @Test
    void restoreAllMerchantDocuments_PropagatesResponse() {
        com.sanedge.gateway.dto.MerchantDocumentDto.SimpleResponse proto =
                new com.sanedge.gateway.dto.MerchantDocumentDto.SimpleResponse("success", "restored all");
        lenient().when(merchantDocumentCommandService.restoreAll(any(com.google.protobuf.Empty.class)))
                .thenAnswer(inv -> Uni.createFrom().item(
                        pb.merchant_document.MerchantDocumentCommand.ApiResponseMerchantDocumentAll.newBuilder()
                                .setStatus("success").setMessage("restored all").build()));
        MerchantDocumentDto.SimpleResponse result = service.restoreAllMerchantDocuments().await().indefinitely();
        assertThat(result.message()).isEqualTo("restored all");
    }
}

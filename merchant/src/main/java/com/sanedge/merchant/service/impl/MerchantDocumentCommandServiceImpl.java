package com.sanedge.merchant.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.CreateMerchantDocumentRequest;
import com.sanedge.merchant.domain.requests.UpdateMerchantDocumentRequest;
import com.sanedge.merchant.domain.requests.UpdateMerchantDocumentStatus;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.MerchantDocumentResponse;
import com.sanedge.merchant.domain.response.MerchantDocumentResponseDeleteAt;
import com.sanedge.merchant.entity.MerchantDocument;
import com.sanedge.common.exception.ResourceNotFoundException;
import com.sanedge.merchant.repository.MerchantQueryRepository;
import com.sanedge.merchant.repository.MerchantDocumentCommandRepository;
import com.sanedge.merchant.repository.MerchantDocumentQueryRepository;
import com.sanedge.merchant.service.MerchantDocumentCommandService;

import io.opentelemetry.api.common.Attributes;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantDocumentCommandServiceImpl implements MerchantDocumentCommandService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantDocumentCommandServiceImpl.class);

    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantDocumentQueryRepository merchantDocumentQueryRepository;
    private final MerchantDocumentCommandRepository merchantDocumentCommandRepository;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    @Inject
    public MerchantDocumentCommandServiceImpl(
                    MerchantQueryRepository merchantQueryRepository,
                    MerchantDocumentQueryRepository merchantDocumentQueryRepository,
                    MerchantDocumentCommandRepository merchantDocumentCommandRepository,
                    RedisService redisService,
                    TracingMetrics tracingMetrics) {
        this.merchantQueryRepository = merchantQueryRepository;
        this.merchantDocumentQueryRepository = merchantDocumentQueryRepository;
        this.merchantDocumentCommandRepository = merchantDocumentCommandRepository;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantDocumentResponse>> create(CreateMerchantDocumentRequest req) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "create")
                        .put("service.name", "merchant-document-command-service")
                        .put("merchant.id", String.valueOf(req.getMerchantId()))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantDocumentResponse>>traceAndMeasure(
                        "createMerchantDocument", "create", attrs, () -> {
                                logger.info("📄 Creating merchant document | MerchantId: {}, Type: {}", req.getMerchantId(),
                                                req.getDocumentType());

                                return merchantQueryRepository.findMerchantById(req.getMerchantId())
                                                .chain(merchant -> {
                                                        if (merchant == null) {
                                                                logger.error("ResourceNotFound: Merchant not found with id {}",
                                                                                req.getMerchantId());
                                                                throw new ResourceNotFoundException("Merchant not found");
                                                        }

                                                        MerchantDocument doc = new MerchantDocument();
                                                        doc.setMerchantId(req.getMerchantId().intValue());
                                                        doc.setDocumentType(req.getDocumentType());
                                                        doc.setDocumentUrl(req.getDocumentUrl());
                                                        doc.setStatus("PENDING");

                                                        return merchantDocumentCommandRepository.persist(doc)
                                                                        .chain(savedDoc -> {
                                                                                logger.info("✅ Merchant document created successfully | Id: {}",
                                                                                                savedDoc.getDocumentId());
                                                                                return Uni.createFrom().item(ApiResponse.success(
                                                                                                "Merchant document created successfully",
                                                                                                MerchantDocumentResponse.from(savedDoc)));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to create merchant document", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantDocumentResponse>> update(UpdateMerchantDocumentRequest req) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "update")
                        .put("service.name", "merchant-document-command-service")
                        .put("doc.id", String.valueOf(req.getDocumentId()))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantDocumentResponse>>traceAndMeasure(
                        "updateMerchantDocument", "update", attrs, () -> {
                                logger.info("🛠️ Updating merchant document | Id: {}", req.getDocumentId());

                                return merchantDocumentQueryRepository.findDocumentById(req.getDocumentId())
                                                .chain(doc -> {
                                                        if (doc == null) {
                                                                logger.error("❌ Merchant document not found with id {}",
                                                                                req.getDocumentId());
                                                                throw new ResourceNotFoundException("Merchant document not found");
                                                        }

                                                        return merchantQueryRepository.findMerchantById(req.getMerchantId())
                                                                        .chain(merchant -> {
                                                                                if (merchant == null) {
                                                                                        logger.error("❌ Merchant not found with id {}",
                                                                                                        req.getMerchantId());
                                                                                        throw new ResourceNotFoundException("Merchant not found");
                                                                                }

                                                                                doc.setMerchantId(req.getMerchantId().intValue());
                                                                                doc.setDocumentType(req.getDocumentType());
                                                                                doc.setDocumentUrl(req.getDocumentUrl());
                                                                                doc.setNote(req.getNote());
                                                                                doc.setStatus(req.getStatus());

                                                                                return merchantDocumentCommandRepository.persist(doc)
                                                                                                .chain(savedDoc -> {
                                                                                                        String cacheKey = "merchant_doc:id:" + req.getDocumentId();

                                                                                                        return redisService.deleteReactive(cacheKey)
                                                                                                                        .map(v -> {
                                                                                                                                logger.info(
                                                                                                                                                "✅ Merchant document updated successfully | Id: {}",
                                                                                                                                                req.getDocumentId());
                                                                                                                                return ApiResponse.success(
                                                                                                                                                "Merchant document updated successfully",
                                                                                                                                                MerchantDocumentResponse.from(savedDoc));
                                                                                                                        });
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to update merchant document", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantDocumentResponse>> updateStatus(UpdateMerchantDocumentStatus req) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "update_status")
                        .put("service.name", "merchant-document-command-service")
                        .put("doc.id", String.valueOf(req.getDocumentId()))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantDocumentResponse>>traceAndMeasure(
                        "updateMerchantDocumentStatus", "update_status", attrs, () -> {
                                logger.info("🛠️ Updating merchant document status | Id: {}", req.getDocumentId());

                                return merchantDocumentQueryRepository.findDocumentById(req.getDocumentId())
                                                .chain(doc -> {
                                                        if (doc == null) {
                                                                logger.error("❌ Merchant document not found with id {}",
                                                                                req.getDocumentId());
                                                                throw new ResourceNotFoundException("Merchant document not found");
                                                        }

                                                        return merchantQueryRepository.findMerchantById(req.getMerchantId())
                                                                        .chain(merchant -> {
                                                                                if (merchant == null) {
                                                                                        logger.error("❌ Merchant not found with id {}",
                                                                                                        req.getMerchantId());
                                                                                        throw new ResourceNotFoundException("Merchant not found");
                                                                                }

                                                                                doc.setStatus(req.getStatus());
                                                                                doc.setNote(req.getNote());

                                                                                return merchantDocumentCommandRepository.persist(doc)
                                                                                                .chain(savedDoc -> {
                                                                                                        String cacheKey = "merchant_doc:id:" + req.getDocumentId();

                                                                                                        return redisService.deleteReactive(cacheKey)
                                                                                                                        .map(v -> {
                                                                                                                                logger.info(
                                                                                                                                                "✅ Merchant document status updated successfully | Id: {}",
                                                                                                                                                req.getDocumentId());
                                                                                                                                return ApiResponse.success(
                                                                                                                                                "Merchant document status updated successfully",
                                                                                                                                                MerchantDocumentResponse.from(savedDoc));
                                                                                                                        });
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to update merchant document status", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantDocumentResponseDeleteAt>> trash(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "trash")
                        .put("service.name", "merchant-document-command-service")
                        .put("doc.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantDocumentResponseDeleteAt>>traceAndMeasure(
                        "trashMerchantDocument", "trash", attrs, () -> {
                                logger.info("🗑️ Trashing merchant document | Id: {}", id);

                                return merchantDocumentCommandRepository.trashed(id)
                                                .chain(doc -> {
                                                        if (doc == null) {
                                                                logger.error("❌ Merchant document not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant document not found");
                                                        }

                                                        String cacheKey = "merchant_doc:id:" + id;

                                                        return redisService.deleteReactive(cacheKey)
                                                                        .map(v -> {
                                                                                logger.info("✅ Merchant document trashed successfully | Id: {}", id);
                                                                                return ApiResponse.success("Merchant document trashed successfully",
                                                                                                MerchantDocumentResponseDeleteAt.from(doc));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to trash merchant document", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantDocumentResponseDeleteAt>> restore(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "restore")
                        .put("service.name", "merchant-document-command-service")
                        .put("doc.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantDocumentResponseDeleteAt>>traceAndMeasure(
                        "restoreMerchantDocument", "restore", attrs, () -> {
                                logger.info("♻️ Restoring merchant document | Id: {}", id);

                                return merchantDocumentCommandRepository.restore(id)
                                                .chain(doc -> {
                                                        if (doc == null) {
                                                                logger.error("❌ Merchant document not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant document not found");
                                                        }

                                                        String cacheKey = "merchant_doc:id:" + id;

                                                        return redisService.deleteReactive(cacheKey)
                                                                        .map(v -> {
                                                                                logger.info("✅ Merchant document restored successfully | Id: {}", id);
                                                                                return ApiResponse.success("Merchant document restored successfully",
                                                                                                MerchantDocumentResponseDeleteAt.from(doc));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to restore merchant document", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deletePermanent(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "delete_permanent")
                        .put("service.name", "merchant-document-command-service")
                        .put("doc.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "deletePermanentMerchantDocument", "delete_permanent", attrs, () -> {
                                logger.info("🗑️ Permanently deleting merchant document | Id: {}", id);

                                return merchantDocumentCommandRepository.deletePermanent(id)
                                                .chain(success -> {
                                                        if (!success) {
                                                                logger.error("❌ Merchant document not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant document not found");
                                                        }

                                                        String cacheKey = "merchant_doc:id:" + id;

                                                        return redisService.deleteReactive(cacheKey)
                                                                        .map(v -> {
                                                                                logger.info("✅ Merchant document permanently deleted | Id: {}", id);
                                                                                return ApiResponse.success("Merchant document permanently deleted", true);
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to permanently delete merchant document", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> restoreAll() {
        Attributes attrs = Attributes.builder()
                        .put("operation", "restore_all")
                        .put("service.name", "merchant-document-command-service")
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "restoreAllMerchantDocuments", "restore_all", attrs, () -> {
                                logger.info("♻️ Restoring all merchant documents");

                                return merchantDocumentCommandRepository.restoreAllDeleted()
                                                .map(success -> ApiResponse.success("All merchant documents restored successfully", success));
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to restore all merchant documents", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteAllPermanent() {
        Attributes attrs = Attributes.builder()
                        .put("operation", "delete_all_permanent")
                        .put("service.name", "merchant-document-command-service")
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "deleteAllPermanentMerchantDocuments", "delete_all_permanent", attrs, () -> {
                                logger.info("🗑️ Permanently deleting all trashed merchant documents");

                                return merchantDocumentCommandRepository.deleteAllDeleted()
                                                .map(success -> ApiResponse.success("All trashed merchant documents permanently deleted", success));
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to permanently delete all merchant documents", e);
                        });
    }
}

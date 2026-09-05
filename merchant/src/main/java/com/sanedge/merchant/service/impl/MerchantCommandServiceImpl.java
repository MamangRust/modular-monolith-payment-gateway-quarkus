package com.sanedge.merchant.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sanedge.common.config.RedisService;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.merchant.domain.requests.CreateMerchantRequest;
import com.sanedge.merchant.domain.requests.UpdateMerchantRequest;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.merchant.domain.response.MerchantResponse;
import com.sanedge.merchant.domain.response.MerchantResponseDeleteAt;
import com.sanedge.merchant.entity.Merchant;
import com.sanedge.common.enums.Status;
import com.sanedge.common.exception.ResourceAlreadyExistsException;
import com.sanedge.common.exception.ResourceNotFoundException;
import pb.user.UserQueryService;
import io.quarkus.grpc.GrpcClient;
import com.sanedge.merchant.repository.MerchantCommandRepository;
import com.sanedge.merchant.repository.MerchantQueryRepository;
import com.sanedge.merchant.service.MerchantCommandService;
import com.sanedge.common.utils.ApiKeyGenerator;

import io.opentelemetry.api.common.Attributes;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MerchantCommandServiceImpl implements MerchantCommandService {
    private static final Logger logger = LoggerFactory.getLogger(MerchantCommandServiceImpl.class);

    private final UserQueryService userQueryService;
    private final MerchantQueryRepository merchantQueryRepository;
    private final MerchantCommandRepository merchantCommandRepository;
    private final RedisService redisService;
    private final TracingMetrics tracingMetrics;

    @Inject
    public MerchantCommandServiceImpl(
                    @GrpcClient("user") UserQueryService userQueryService,
                    MerchantQueryRepository merchantQueryRepository,
                    MerchantCommandRepository merchantCommandRepository,
                    RedisService redisService,
                    TracingMetrics tracingMetrics) {
        this.userQueryService = userQueryService;
        this.merchantQueryRepository = merchantQueryRepository;
        this.merchantCommandRepository = merchantCommandRepository;
        this.redisService = redisService;
        this.tracingMetrics = tracingMetrics;
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantResponse>> createMerchant(CreateMerchantRequest req) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "create_merchant")
                        .put("service.name", "merchant-command-service")
                        .put("merchant.name", req.getName())
                        .build();

        return tracingMetrics.<ApiResponse<MerchantResponse>>traceAndMeasure(
                        "createMerchant", "create_merchant", attrs, () -> {
                                return userQueryService.findById(pb.user.User.FindByIdUserRequest.newBuilder()
                                                .setId(req.getUserId().intValue()).build())
                                                .chain(response -> {
                                                        if (response == null || !response.hasData()) {
                                                                logger.error("❌ User not found with id {}", req.getUserId());
                                                                throw new ResourceNotFoundException("User not found");
                                                        }
                                                        return merchantQueryRepository.existsByName(req.getName());
                                                })
                                                .chain(nameExists -> {
                                                        if (nameExists) {
                                                                logger.error("❌ Merchant name already taken | Name: {}", req.getName());
                                                                throw new ResourceAlreadyExistsException("Merchant name already taken");
                                                        }

                                                        String apiKey = ApiKeyGenerator.generateApiKey();
                                                        UUID merchantNo = UUID.randomUUID();

                                                        Merchant merchant = new Merchant();
                                                        merchant.setName(req.getName());
                                                        merchant.setMerchantNo(merchantNo);
                                                        merchant.setUserId(req.getUserId().intValue());
                                                        merchant.setApiKey(apiKey);
                                                        merchant.setStatus(Status.PENDING);

                                                        return merchantCommandRepository.persist(merchant)
                                                                        .chain(savedMerchant -> {
                                                                                logger.info("✅ Merchant created successfully | Id: {}, ApiKey: {}", merchant.getMerchantId(), apiKey);
                                                                                return Uni.createFrom()
                                                                                                .item(ApiResponse.success("Merchant created successfully", MerchantResponse.from(merchant)));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to create merchant | Name: {}, UserId: {}", req.getName(), req.getUserId(), e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantResponse>> updateMerchant(UpdateMerchantRequest req) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "update_merchant")
                        .put("service.name", "merchant-command-service")
                        .put("merchant.id", String.valueOf(req.getMerchantId()))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantResponse>>traceAndMeasure(
                        "updateMerchant", "update_merchant", attrs, () -> {
                                return merchantQueryRepository.findMerchantById(req.getMerchantId())
                                                .chain(merchant -> {
                                                        if (merchant == null) {
                                                                logger.error("❌ Merchant not found with id {}", req.getMerchantId());
                                                                throw new ResourceNotFoundException("Merchant not found");
                                                        }

                                                        Uni<Void> userCheckUni = Uni.createFrom().nullItem();
                                                        if (req.getUserId() != null) {
                                                                userCheckUni = userQueryService.findById(pb.user.User.FindByIdUserRequest.newBuilder()
                                                                                .setId(req.getUserId().intValue()).build())
                                                                                .chain(response -> {
                                                                                        if (response == null || !response.hasData()) {
                                                                                                logger.error("❌ User not found with id {}", req.getUserId());
                                                                                                throw new ResourceNotFoundException("User not found");
                                                                                        }
                                                                                        merchant.setUserId(req.getUserId().intValue());
                                                                                        return Uni.createFrom().nullItem();
                                                                                });
                                                        }

                                                        return userCheckUni.chain(v -> {
                                                                merchant.setName(req.getName());
                                                                merchant.setStatus(Status.valueOf(req.getStatus().toUpperCase()));

                                                                return merchantCommandRepository.persist(merchant)
                                                                                .chain(savedMerchant -> {
                                                                                        String cacheIdKey = "merchant:id:" + req.getMerchantId();
                                                                                        String cacheApiKey = "merchant:apikey:" + merchant.getApiKey();
                                                                                        String cacheUserKey = "merchant:user:" + merchant.getUserId();

                                                                                        return Uni.combine().all().unis(
                                                                                                        redisService.deleteReactive(cacheIdKey),
                                                                                                        redisService.deleteReactive(cacheApiKey),
                                                                                                        redisService.deleteReactive(cacheUserKey))
                                                                                                        .asTuple().map(v2 -> {
                                                                                                                logger.info("✅ Merchant updated successfully | Id: {}", req.getMerchantId());
                                                                                                                return ApiResponse.success("Merchant updated successfully",
                                                                                                                                MerchantResponse.from(merchant));
                                                                                                        });
                                                                                });
                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to update merchant | Id: {}", req.getMerchantId(), e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantResponseDeleteAt>> trashMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "trash_merchant")
                        .put("service.name", "merchant-command-service")
                        .put("merchant.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantResponseDeleteAt>>traceAndMeasure(
                        "trashMerchant", "trash_merchant", attrs, () -> {
                                logger.info("🗑️ Trashing merchant id={}", id);

                                return merchantCommandRepository.trashed(id)
                                                .chain(merchant -> {
                                                        if (merchant == null) {
                                                                logger.error("❌ Merchant not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant not found");
                                                        }

                                                        String cacheIdKey = "merchant:id:" + id;
                                                        String cacheApiKey = "merchant:apikey:" + merchant.getApiKey();
                                                        String cacheUserKey = "merchant:user:" + merchant.getUserId();

                                                        return Uni.combine().all().unis(
                                                                        redisService.deleteReactive(cacheIdKey),
                                                                        redisService.deleteReactive(cacheApiKey),
                                                                        redisService.deleteReactive(cacheUserKey))
                                                                        .asTuple().map(v2 -> {
                                                                                logger.info("✅ Merchant trashed successfully | Id: {}", id);
                                                                                return ApiResponse.success("Merchant trashed successfully",
                                                                                                MerchantResponseDeleteAt.from(merchant));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to trash merchant id={}", id, e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<MerchantResponseDeleteAt>> restoreMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "restore_merchant")
                        .put("service.name", "merchant-command-service")
                        .put("merchant.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<MerchantResponseDeleteAt>>traceAndMeasure(
                        "restoreMerchant", "restore_merchant", attrs, () -> {
                                logger.info("♻️ Restoring merchant id={}", id);

                                return merchantCommandRepository.restore(id)
                                                .chain(merchant -> {
                                                        if (merchant == null) {
                                                                logger.error("❌ Merchant not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant not found");
                                                        }

                                                        String cacheIdKey = "merchant:id:" + id;
                                                        String cacheApiKey = "merchant:apikey:" + merchant.getApiKey();
                                                        String cacheUserKey = "merchant:user:" + merchant.getUserId();

                                                        return Uni.combine().all().unis(
                                                                        redisService.deleteReactive(cacheIdKey),
                                                                        redisService.deleteReactive(cacheApiKey),
                                                                        redisService.deleteReactive(cacheUserKey))
                                                                        .asTuple().map(v2 -> {
                                                                                logger.info("✅ Merchant restored successfully | Id: {}", id);
                                                                                return ApiResponse.success("Merchant restored successfully",
                                                                                                MerchantResponseDeleteAt.from(merchant));
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to restore merchant id={}", id, e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteMerchant(Long id) {
        Attributes attrs = Attributes.builder()
                        .put("operation", "delete_merchant")
                        .put("service.name", "merchant-command-service")
                        .put("merchant.id", String.valueOf(id))
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "deleteMerchant", "delete_merchant", attrs, () -> {
                                logger.info("🧨 Permanently deleting merchant id={}", id);

                                return merchantQueryRepository.findMerchantById(id)
                                                .chain(merchant -> {
                                                        if (merchant == null) {
                                                                logger.error("❌ Merchant not found with id {}", id);
                                                                throw new ResourceNotFoundException("Merchant not found");
                                                        }

                                                        String cacheIdKey = "merchant:id:" + id;
                                                        String cacheApiKey = "merchant:apikey:" + merchant.getApiKey();
                                                        String cacheUserKey = "merchant:user:" + merchant.getUserId();

                                                        return merchantCommandRepository.deletePermanent(id)
                                                                        .chain(deleted -> {
                                                                                return Uni.combine().all().unis(
                                                                                                redisService.deleteReactive(cacheIdKey),
                                                                                                redisService.deleteReactive(cacheApiKey),
                                                                                                redisService.deleteReactive(cacheUserKey))
                                                                                                .asTuple().map(v2 -> {
                                                                                                        logger.info("✅ Merchant permanently deleted | Id: {}", id);
                                                                                                        return ApiResponse.success("Merchant permanently deleted", true);
                                                                                                });
                                                                        });
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to permanently delete merchant id={}", id, e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> restoreAll() {
        Attributes attrs = Attributes.builder()
                        .put("operation", "restore_all_merchants")
                        .put("service.name", "merchant-command-service")
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "restoreAllMerchants", "restore_all_merchants", attrs, () -> {
                                logger.info("🔄 Restoring ALL trashed merchants");

                                return merchantCommandRepository.restoreAllDeleted()
                                                .map(restored -> {
                                                        logger.info("✅ Restored all trashed merchants");
                                                        return ApiResponse.success("Restored all trashed merchants", restored);
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to restore all merchants", e);
                        });
    }

    @Override
    @WithTransaction
    public Uni<ApiResponse<Boolean>> deleteAll() {
        Attributes attrs = Attributes.builder()
                        .put("operation", "delete_all_merchants")
                        .put("service.name", "merchant-command-service")
                        .build();

        return tracingMetrics.<ApiResponse<Boolean>>traceAndMeasure(
                        "deleteAllMerchants", "delete_all_merchants", attrs, () -> {
                                logger.info("💣 Permanently deleting ALL trashed merchants");

                                return merchantCommandRepository.deleteAllDeleted()
                                                .map(deleted -> {
                                                        logger.info("✅ Deleted all trashed merchants");
                                                        return ApiResponse.success("Deleted all trashed merchants", deleted);
                                                });
                        }).onFailure().invoke(e -> {
                                logger.error("💥 Failed to delete all merchants", e);
                        });
    }
}

package com.sanedge.transfer.repository;

import com.sanedge.transfer.entity.Transfer;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransferCommandRepository implements PanacheRepository<Transfer> {

    @WithTransaction
    public Uni<Transfer> updateTransferAmount(Long transferId, Long amount) {
        return find("transferId = ?1 AND deletedAt IS NULL", transferId).firstResult()
                .chain(transfer -> {
                    if (transfer != null) {
                        transfer.transferAmount = amount != null ? amount.intValue() : 0;
                        transfer.transferTime = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
                        transfer.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(transfer).map(v -> transfer);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Transfer> updateTransferStatus(Long transferId, String status) {
        return find("transferId = ?1 AND deletedAt IS NULL", transferId).firstResult()
                .chain(transfer -> {
                    if (transfer != null) {
                        try {
                            transfer.status = com.sanedge.common.enums.Status.valueOf(status.toUpperCase());
                            transfer.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                            return persist(transfer).map(v -> transfer);
                        } catch (IllegalArgumentException e) {
                            return Uni.createFrom().item(transfer);
                        }
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Transfer> trashed(Long transferId) {
        return find("transferId = ?1 AND deletedAt IS NULL", transferId).firstResult()
                .chain(transfer -> {
                    if (transfer != null) {
                        transfer.setDeletedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(transfer).map(v -> transfer);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Transfer> restore(Long transferId) {
        return find("transferId = ?1 AND deletedAt IS NOT NULL", transferId).firstResult()
                .chain(transfer -> {
                    if (transfer != null) {
                        transfer.setDeletedAt(null);
                        return persist(transfer).map(v -> transfer);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Boolean> deletePermanent(Long transferId) {
        return find("transferId = ?1", transferId).firstResult()
                .chain(transfer -> {
                    if (transfer != null) {
                        return delete(transfer).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> restoreAllDeleted() {
        return update("deletedAt = NULL WHERE deletedAt IS NOT NULL")
                .map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> deleteAllDeleted() {
        return delete("deletedAt IS NOT NULL")
                .map(count -> count > 0);
    }

    @WithTransaction
    public Uni<Boolean> markCompensationRequired(Long transferId, String reason) {
        return find("transferId = ?1 AND deletedAt IS NULL", transferId).firstResult()
                .chain(t -> {
                    if (t != null) {
                        t.compensationReason = reason;
                        t.compensationAttempts = t.compensationAttempts != null ? t.compensationAttempts : 0;
                        t.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(t).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> claimCompensation(Long transferId, String workerId, String claimToken,
            java.sql.Timestamp now, java.sql.Timestamp leaseUntil, int maxAttempts) {
        return find("transferId = ?1 AND (compensationWorkerId IS NULL OR compensationLeaseUntil < ?2)",
                transferId, now).firstResult()
                .chain(t -> {
                    if (t != null) {
                        int attempts = t.compensationAttempts != null ? t.compensationAttempts : 0;
                        if (attempts >= maxAttempts) return Uni.createFrom().item(false);
                        t.compensationWorkerId = workerId;
                        t.compensationClaimToken = claimToken;
                        t.compensationLeaseUntil = leaseUntil;
                        t.compensationAttempts = attempts + 1;
                        t.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(t).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> completeCompensation(Long transferId, String workerId, String claimToken) {
        return find("transferId = ?1 AND compensationClaimToken = ?2", transferId, claimToken).firstResult()
                .chain(t -> {
                    if (t != null && workerId.equals(t.compensationWorkerId)) {
                        t.compensationWorkerId = null;
                        t.compensationClaimToken = null;
                        t.compensationLeaseUntil = null;
                        t.status = com.sanedge.common.enums.Status.SUCCESS;
                        t.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(t).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> releaseCompensation(Long transferId, String workerId, String claimToken,
            java.sql.Timestamp leaseUntil, String reason) {
        return find("transferId = ?1 AND compensationClaimToken = ?2", transferId, claimToken).firstResult()
                .chain(t -> {
                    if (t != null && workerId.equals(t.compensationWorkerId)) {
                        t.compensationWorkerId = null;
                        t.compensationClaimToken = null;
                        t.compensationLeaseUntil = leaseUntil;
                        t.compensationReason = reason;
                        t.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(t).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> exhaustCompensation(Long transferId, int maxAttempts, String reason) {
        return find("transferId = ?1", transferId).firstResult()
                .chain(t -> {
                    if (t != null) {
                        t.compensationAttempts = maxAttempts;
                        t.compensationReason = reason;
                        t.status = com.sanedge.common.enums.Status.FAILED;
                        t.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(t).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }
}

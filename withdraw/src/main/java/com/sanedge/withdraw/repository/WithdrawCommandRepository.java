package com.sanedge.withdraw.repository;

import com.sanedge.withdraw.entity.Withdraw;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WithdrawCommandRepository implements PanacheRepository<Withdraw> {

    @WithTransaction
    public Uni<Withdraw> updateStatus(Long withdrawId, String status) {
        return find("withdrawId = ?1 AND deletedAt IS NULL", withdrawId).firstResult()
                .chain(withdraw -> {
                    if (withdraw != null) {
                        try {
                            withdraw.status = com.sanedge.common.enums.Status.valueOf(status.toUpperCase());
                            withdraw.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                            return persist(withdraw).map(v -> withdraw);
                        } catch (IllegalArgumentException e) {
                            return Uni.createFrom().item(withdraw);
                        }
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Withdraw> trashed(Long withdrawId) {
        return find("withdrawId = ?1 AND deletedAt IS NULL", withdrawId).firstResult()
                .chain(withdraw -> {
                    if (withdraw != null) {
                        withdraw.setDeletedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(withdraw).map(v -> withdraw);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Withdraw> restore(Long withdrawId) {
        return find("withdrawId = ?1 AND deletedAt IS NOT NULL", withdrawId).firstResult()
                .chain(withdraw -> {
                    if (withdraw != null) {
                        withdraw.setDeletedAt(null);
                        return persist(withdraw).map(v -> withdraw);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Boolean> deletePermanent(Long withdrawId) {
        return find("withdrawId = ?1", withdrawId).firstResult()
                .chain(withdraw -> {
                    if (withdraw != null) {
                        return delete(withdraw).map(v -> true);
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
    public Uni<Boolean> markCompensationRequired(Long withdrawId, String reason) {
        return find("withdrawId = ?1 AND deletedAt IS NULL", withdrawId).firstResult()
                .chain(w -> {
                    if (w != null) {
                        w.compensationAttempts = 1;
                        w.compensationReason = reason;
                        w.compensationWorkerId = null;
                        w.compensationClaimToken = null;
                        w.compensationLeaseUntil = null;
                        w.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(w).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> claimCompensation(Long withdrawId, String workerId, String claimToken,
            java.sql.Timestamp now, java.sql.Timestamp leaseUntil, int maxAttempts) {
        return find("withdrawId = ?1 AND (compensationWorkerId IS NULL OR compensationLeaseUntil < ?2)",
                withdrawId, now).firstResult()
                .chain(w -> {
                    if (w != null) {
                        int attempts = w.compensationAttempts != null ? w.compensationAttempts : 0;
                        if (attempts >= maxAttempts) return Uni.createFrom().item(false);
                        // Only bump the attempt counter when reclaiming an already-claimed
                        // record (i.e. its previous lease expired). The first claim merely
                        // establishes the lease and keeps the counter set by markCompensationRequired.
                        boolean reclaim = w.compensationWorkerId != null;
                        w.compensationWorkerId = workerId;
                        w.compensationClaimToken = claimToken;
                        w.compensationLeaseUntil = leaseUntil;
                        if (reclaim) {
                            w.compensationAttempts = attempts + 1;
                        } else if (attempts < 1) {
                            w.compensationAttempts = 1;
                        }
                        w.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(w).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> completeCompensation(Long withdrawId, String workerId, String claimToken) {
        return find("withdrawId = ?1 AND compensationClaimToken = ?2", withdrawId, claimToken).firstResult()
                .chain(w -> {
                    if (w != null && workerId.equals(w.compensationWorkerId)) {
                        w.compensationWorkerId = null;
                        w.compensationClaimToken = null;
                        w.compensationLeaseUntil = null;
                        w.status = com.sanedge.common.enums.Status.COMPENSATED;
                        w.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(w).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> releaseCompensation(Long withdrawId, String workerId, String claimToken,
            java.sql.Timestamp leaseUntil, String reason) {
        return find("withdrawId = ?1 AND compensationClaimToken = ?2", withdrawId, claimToken).firstResult()
                .chain(w -> {
                    if (w != null && workerId.equals(w.compensationWorkerId)) {
                        w.compensationWorkerId = null;
                        w.compensationClaimToken = null;
                        w.compensationLeaseUntil = leaseUntil;
                        w.compensationReason = reason;
                        w.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(w).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> exhaustCompensation(Long withdrawId, int maxAttempts, String reason) {
        return find("withdrawId = ?1", withdrawId).firstResult()
                .chain(w -> {
                    if (w != null) {
                        w.compensationAttempts = maxAttempts;
                        w.compensationReason = reason;
                        w.status = com.sanedge.common.enums.Status.FAILED;
                        w.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(w).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }
}

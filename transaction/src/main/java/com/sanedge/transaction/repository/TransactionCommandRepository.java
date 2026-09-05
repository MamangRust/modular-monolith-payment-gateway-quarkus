package com.sanedge.transaction.repository;

import com.sanedge.transaction.entity.Transaction;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionCommandRepository implements PanacheRepository<Transaction> {

    @WithTransaction
    public Uni<Transaction> updateTransactionStatus(Long transactionId, String status) {
        return find("transactionId = ?1 AND deletedAt IS NULL", transactionId).firstResult()
                .chain(transaction -> {
                    if (transaction != null) {
                        try {
                            transaction.status = com.sanedge.common.enums.Status.valueOf(status.toUpperCase());
                            transaction.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                            return persist(transaction).map(v -> transaction);
                        } catch (IllegalArgumentException e) {
                            return Uni.createFrom().item(transaction);
                        }
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Transaction> trashed(Long transactionId) {
        return find("transactionId = ?1 AND deletedAt IS NULL", transactionId).firstResult()
                .chain(transaction -> {
                    if (transaction != null) {
                        transaction.setDeletedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(transaction).map(v -> transaction);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Transaction> restore(Long transactionId) {
        return find("transactionId = ?1 AND deletedAt IS NOT NULL", transactionId).firstResult()
                .chain(transaction -> {
                    if (transaction != null) {
                        transaction.setDeletedAt(null);
                        return persist(transaction).map(v -> transaction);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Boolean> deletePermanent(Long transactionId) {
        return find("transactionId = ?1", transactionId).firstResult()
                .chain(transaction -> {
                    if (transaction != null) {
                        return delete(transaction).map(v -> true);
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
    public Uni<Transaction> markCompensationRequired(Long transactionId, String reason) {
        return find("transactionId = ?1 AND deletedAt IS NULL", transactionId).firstResult()
                .chain(txn -> {
                    if (txn != null) {
                        txn.compensationReason = reason;
                        txn.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(txn).map(v -> txn);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Boolean> claimCompensation(Long transactionId, String workerId, String claimToken,
            java.sql.Timestamp now, java.sql.Timestamp leaseUntil, int maxAttempts) {
        return find("transactionId = ?1 AND (compensationWorkerId IS NULL OR compensationLeaseUntil < ?2)",
                transactionId, now).firstResult()
                .chain(txn -> {
                    if (txn != null) {
                        int attempts = txn.compensationAttempts != null ? txn.compensationAttempts : 0;
                        if (attempts >= maxAttempts) return Uni.createFrom().item(false);
                        txn.compensationWorkerId = workerId;
                        txn.compensationClaimToken = claimToken;
                        txn.compensationLeaseUntil = leaseUntil;
                        txn.compensationAttempts = attempts + 1;
                        txn.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(txn).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> completeCompensation(Long transactionId, String workerId, String claimToken) {
        return find("transactionId = ?1 AND compensationClaimToken = ?2", transactionId, claimToken).firstResult()
                .chain(txn -> {
                    if (txn != null && workerId.equals(txn.compensationWorkerId)) {
                        txn.compensationWorkerId = null;
                        txn.compensationClaimToken = null;
                        txn.compensationLeaseUntil = null;
                        txn.status = com.sanedge.common.enums.Status.SUCCESS;
                        txn.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(txn).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> releaseCompensation(Long transactionId, String workerId, String claimToken,
            java.sql.Timestamp leaseUntil, String reason) {
        return find("transactionId = ?1 AND compensationClaimToken = ?2", transactionId, claimToken).firstResult()
                .chain(txn -> {
                    if (txn != null && workerId.equals(txn.compensationWorkerId)) {
                        txn.compensationWorkerId = null;
                        txn.compensationClaimToken = null;
                        txn.compensationLeaseUntil = leaseUntil;
                        txn.compensationReason = reason;
                        txn.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(txn).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }

    @WithTransaction
    public Uni<Boolean> exhaustCompensation(Long transactionId, int maxAttempts, String reason) {
        return find("transactionId = ?1", transactionId).firstResult()
                .chain(txn -> {
                    if (txn != null) {
                        txn.compensationAttempts = maxAttempts;
                        txn.compensationReason = reason;
                        txn.status = com.sanedge.common.enums.Status.FAILED;
                        txn.setUpdatedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(txn).map(v -> true);
                    }
                    return Uni.createFrom().item(false);
                });
    }
}

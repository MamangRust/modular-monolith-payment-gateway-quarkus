package com.sanedge.saldo.repository;

import com.sanedge.saldo.entity.Saldo;
import com.sanedge.saldo.entity.SaldoMutationOperation;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SaldoCommandRepository implements PanacheRepository<Saldo> {

    @Inject
    SaldoMutationOperationRepository saldoMutationOperationRepository;

    @WithTransaction
    public Uni<Integer> updateBalanceByDelta(String cardNumber, long delta, long minimumBalance, String operationKey) {
        java.sql.Timestamp now = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());

        return saldoMutationOperationRepository.findByOperationKey(operationKey)
                .chain(existing -> {
                    // Exactly-once: an already APPLIED operation is never re-applied.
                    if (existing != null && "APPLIED".equals(existing.getResultStatus())) {
                        return Uni.createFrom().item(1);
                    }

                    return find("cardNumber = ?1 AND deletedAt IS NULL", cardNumber).firstResult()
                            .chain(saldo -> {
                                if (saldo == null) {
                                    return Uni.createFrom().item(0);
                                }

                                long currentBalance = saldo.getTotalBalance() != null ? saldo.getTotalBalance() : 0L;
                                long newBalance = currentBalance + delta;

                                if (newBalance < minimumBalance) {
                                    SaldoMutationOperation op = existing != null ? existing : new SaldoMutationOperation();
                                    op.setOperationKey(operationKey);
                                    op.setCardNumber(cardNumber);
                                    op.setRequestedDelta((int) delta);
                                    op.setMinimumBalance((int) minimumBalance);
                                    op.setResultStatus("REJECTED");
                                    op.setResultBalance((int) currentBalance);
                                    op.setFailureReason("Insufficient balance");
                                    op.setCreatedAt(now);
                                    op.setUpdatedAt(now);
                                    return saldoMutationOperationRepository.persist(op).map(v -> 0);
                                }

                                saldo.setTotalBalance((int) newBalance);
                                saldo.setUpdatedAt(now);

                                return persist(saldo)
                                        .chain(s -> {
                                            SaldoMutationOperation op = existing != null ? existing : new SaldoMutationOperation();
                                            op.setOperationKey(operationKey);
                                            op.setCardNumber(cardNumber);
                                            op.setRequestedDelta((int) delta);
                                            op.setMinimumBalance((int) minimumBalance);
                                            op.setResultStatus("APPLIED");
                                            op.setResultBalance((int) newBalance);
                                            op.setFailureReason(null);
                                            op.setCreatedAt(now);
                                            op.setUpdatedAt(now);
                                            return saldoMutationOperationRepository.persist(op).map(v -> 1);
                                        });
                            });
                });
    }

    @WithTransaction
    public Uni<Saldo> trashed(Long saldoId) {
        return find("saldoId = ?1 AND deletedAt IS NULL", saldoId).firstResult()
                .chain(saldo -> {
                    if (saldo != null) {
                        saldo.setDeletedAt(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
                        return persist(saldo).map(v -> saldo);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Saldo> restore(Long saldoId) {
        return find("saldoId = ?1 AND deletedAt IS NOT NULL", saldoId).firstResult()
                .chain(saldo -> {
                    if (saldo != null) {
                        saldo.setDeletedAt(null);
                        return persist(saldo).map(v -> saldo);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Integer> updateBalanceByCardNumber(String cardNumber, Long newBalance) {
        return update("totalBalance = ?1, updatedAt = ?2 WHERE cardNumber = ?3 AND deletedAt IS NULL",
                newBalance != null ? newBalance.intValue() : 0,
                java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()),
                cardNumber);
    }

    @WithTransaction
    public Uni<Integer> updateWithdrawByCardNumber(String cardNumber, Long withdrawAmount) {
        java.sql.Timestamp now = java.sql.Timestamp.valueOf(java.time.LocalDateTime.now());
        return update("withdrawAmount = ?1, withdrawTime = ?2, updatedAt = ?3 WHERE cardNumber = ?4 AND deletedAt IS NULL",
                withdrawAmount != null ? withdrawAmount.intValue() : 0,
                now,
                now,
                cardNumber);
    }

    @WithTransaction
    public Uni<Boolean> deletePermanent(Long saldoId) {
        return find("saldoId = ?1", saldoId).firstResult()
                .chain(saldo -> {
                    if (saldo != null) {
                        return delete(saldo).map(v -> true);
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
}

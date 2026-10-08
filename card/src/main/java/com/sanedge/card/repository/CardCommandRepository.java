package com.sanedge.card.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.sanedge.card.entity.Card;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CardCommandRepository implements PanacheRepository<Card> {

    @WithTransaction
    public Uni<Card> trashed(Long cardId) {
        return find("cardId = ?1 AND deletedAt IS NULL", cardId).firstResult()
                .chain(card -> {
                    if (card != null) {
                        card.setDeletedAt(new Timestamp(System.currentTimeMillis()));
                        return persist(card).map(v -> card);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Card> restore(Long cardId) {
        return find("cardId = ?1 AND deletedAt IS NOT NULL", cardId).firstResult()
                .chain(card -> {
                    if (card != null) {
                        card.setDeletedAt(null);
                        return persist(card).map(v -> card);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Card> deletePermanent(Long cardId) {
        return find("cardId = ?1 AND deletedAt IS NOT NULL", cardId).firstResult()
                .chain(card -> {
                    if (card != null) {
                        return delete(card).map(v -> card);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Boolean> restoreAllDeleted() {
        return update("deletedAt = NULL WHERE deletedAt IS NOT NULL")
                .map(updatedCount -> updatedCount > 0);
    }

    @WithTransaction
    public Uni<Boolean> deleteAllDeleted() {
        return delete("deletedAt IS NOT NULL")
                .map(deletedCount -> deletedCount > 0);
    }

    @WithTransaction
    public Uni<Card> toggleStatus(Long cardId) {
        return find("cardId = ?1", cardId).firstResult()
                .chain(card -> {
                    if (card != null) {
                        if (card.status == Card.CardStatus.ACTIVE) {
                            card.status = Card.CardStatus.SUSPENDED;
                        } else if (card.status == Card.CardStatus.SUSPENDED) {
                            card.status = Card.CardStatus.ACTIVE;
                        } else {
                            return Uni.createFrom().nullItem();
                        }
                        return persist(card).map(v -> card);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Card> updateCreditLimit(Long cardId, BigDecimal newLimit) {
        return find("cardId = ?1 AND deletedAt IS NULL", cardId).firstResult()
                .chain(card -> {
                    if (card != null) {
                        card.setCreditLimit(newLimit);
                        return persist(card).map(v -> card);
                    }
                    return Uni.createFrom().nullItem();
                });
    }

    @WithTransaction
    public Uni<Card> redeemPoints(Long cardId, BigDecimal points) {
        return find("cardId = ?1 AND deletedAt IS NULL", cardId).firstResult()
                .chain(card -> {
                    if (card == null) {
                        return Uni.createFrom().nullItem();
                    }
                    BigDecimal available = card.getPoints() != null ? card.getPoints() : BigDecimal.ZERO;
                    if (available.compareTo(points) < 0) {
                        return Uni.createFrom().nullItem();
                    }
                    card.setPoints(available.subtract(points));
                    return persist(card).map(v -> card);
                });
    }
}

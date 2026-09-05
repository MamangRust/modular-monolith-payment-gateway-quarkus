package com.sanedge.card.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@QuarkusTest
@RunOnVertxContext
class CardDashboardByCardRepositoryTest {

    @Inject
    CardDashboardByCardRepository repository;

    private final String CARD = "1234-5678-9012-3456";

    @Test
    @WithSession
    Uni<Void> testGetTotalBalanceByCard_ReturnsZeroWhenNoData() {
        return repository.getTotalBalanceByCard(CARD)
                .invoke(balance -> assertThat(balance).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTopupAmountByCard_ReturnsZeroWhenNoData() {
        return repository.getTotalTopupAmountByCard(CARD)
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalWithdrawAmountByCard_ReturnsZeroWhenNoData() {
        return repository.getTotalWithdrawAmountByCard(CARD)
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTransactionAmountByCard_ReturnsZeroWhenNoData() {
        return repository.getTotalTransactionAmountByCard(CARD)
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTransferAmountBySender_ReturnsZeroWhenNoData() {
        return repository.getTotalTransferAmountBySender(CARD)
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTransferAmountByReceiver_ReturnsZeroWhenNoData() {
        return repository.getTotalTransferAmountByReceiver(CARD)
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }
}
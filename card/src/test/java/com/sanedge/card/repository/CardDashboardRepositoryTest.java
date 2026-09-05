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
class CardDashboardRepositoryTest {

    @Inject
    CardDashboardRepository repository;

    @Test
    @WithSession
    Uni<Void> testGetTotalBalance_ReturnsZeroWhenNoData() {
        return repository.getTotalBalance()
                .invoke(balance -> assertThat(balance).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTopupAmount_ReturnsZeroWhenNoData() {
        return repository.getTotalTopupAmount()
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalWithdrawAmount_ReturnsZeroWhenNoData() {
        return repository.getTotalWithdrawAmount()
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTransactionAmount_ReturnsZeroWhenNoData() {
        return repository.getTotalTransactionAmount()
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetTotalTransferAmount_ReturnsZeroWhenNoData() {
        return repository.getTotalTransferAmount()
                .invoke(amount -> assertThat(amount).isZero())
                .replaceWithVoid();
    }
}
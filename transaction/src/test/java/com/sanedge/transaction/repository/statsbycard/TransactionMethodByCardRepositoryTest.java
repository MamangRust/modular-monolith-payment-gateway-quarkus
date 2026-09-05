package com.sanedge.transaction.repository.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class TransactionMethodByCardRepositoryTest {

    @Inject
    TransactionMethodByCardRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyPaymentMethodsByCardReturnsEmptyOrNoError() {
        return repository.findMonthlyPaymentMethodsByCard("NONEXISTENT", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyPaymentMethodsByCardReturnsEmptyOrNoError() {
        return repository.findYearlyPaymentMethodsByCard("NONEXISTENT", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }
}
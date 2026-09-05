package com.sanedge.topup.repository.statsbycard;

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
class TopupMethodByCardRepositoryTest {

    @Inject
    TopupMethodByCardRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTopupMethodsByCardReturnsEmptyWhenNoData() {
        return repository.findMonthlyTopupMethodsByCard("NONEXISTENT", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTopupMethodsByCardReturnsEmptyWhenNoData() {
        return repository.findYearlyTopupMethodsByCardId("NONEXISTENT", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }
}
package com.sanedge.card.repository.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

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
class CardWithdrawAmountRepositoryTest {

    @Inject
    CardWithdrawAmountRepository repository;

    @Test
    @WithSession
    Uni<Void> testGetMonthlyWithdrawAmount_ReturnsTwelveMonthsWithZeroAmount() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        return repository.getMonthlyWithdrawAmount(date)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(0).getMonth()).isEqualTo("Jan");
                    assertThat(result.get(11).getMonth()).isEqualTo("Dec");
                    assertThat(result).allSatisfy(month -> assertThat(month.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetYearlyWithdrawAmount_ReturnsFiveYearsWithZeroAmount() {
        return repository.getYearlyWithdrawAmount(2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(5);
                    assertThat(result.get(0).getYear()).isEqualTo("2020");
                    assertThat(result.get(4).getYear()).isEqualTo("2024");
                    assertThat(result).allSatisfy(year -> assertThat(year.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }
}
package com.sanedge.merchant.repository.stats;

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
class MerchantAmountRepositoryTest {

    @Inject
    MerchantAmountRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyAmountReturnsTwelveMonths() {
        return repository.findMonthlyAmount(2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(0).getMonth()).isEqualTo("Jan");
                    assertThat(result.get(11).getMonth()).isEqualTo("Dec");
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthlyAmountReturnsZeroAmountWhenNoData() {
        return repository.findMonthlyAmount(2024L)
                .invoke(result -> {
                    assertThat(result).allSatisfy(month -> assertThat(month.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyAmountReturnsFiveYears() {
        return repository.findYearlyAmount(2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(5);
                    assertThat(result.get(0).getYear()).isEqualTo("2020");
                    assertThat(result.get(4).getYear()).isEqualTo("2024");
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyAmountReturnsZeroAmountWhenNoData() {
        return repository.findYearlyAmount(2024L)
                .invoke(result -> {
                    assertThat(result).allSatisfy(year -> assertThat(year.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }
}
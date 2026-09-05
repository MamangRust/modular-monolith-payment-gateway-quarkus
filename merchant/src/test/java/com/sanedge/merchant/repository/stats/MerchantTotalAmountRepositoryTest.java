package com.sanedge.merchant.repository.stats;

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
class MerchantTotalAmountRepositoryTest {

    @Inject
    MerchantTotalAmountRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTotalAmount_ReturnsZeroAmountsWhenNoData() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        return repository.findMonthlyTotalAmount(date)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).anySatisfy(item -> {
                        assertThat(item.getMonth()).isIn("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                        assertThat(item.getTotalAmount()).isZero();
                    });
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTotalAmount_ReturnsTwoYearsWithZeroWhenNoData() {
        return repository.findYearlyTotalAmount(2024L, 2023L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                    assertThat(result).allSatisfy(year -> {
                        assertThat(year.getYear()).isIn("2024", "2023");
                        assertThat(year.getTotalAmount()).isZero();
                    });
                })
                .replaceWithVoid();
    }
}
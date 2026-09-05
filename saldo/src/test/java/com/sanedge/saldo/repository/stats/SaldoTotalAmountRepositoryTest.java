package com.sanedge.saldo.repository.stats;

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
class SaldoTotalAmountRepositoryTest {

    @Inject
    SaldoTotalAmountRepository saldoTotalAmountRepository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTotalSaldoBalanceReturnsTwelveMonths() {
        return saldoTotalAmountRepository.findMonthlyTotalSaldoBalance(2024L, 1, 2024L, 12)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result).anySatisfy(month -> {
                        assertThat(month.getMonth()).isIn(
                                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                    });
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTotalSaldoBalanceReturnsZeroWhenNoData() {
        return saldoTotalAmountRepository.findMonthlyTotalSaldoBalance(2024L, 1, 2024L, 12)
                .invoke(result -> {
                    assertThat(result).allSatisfy(month -> assertThat(month.getTotalBalance()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTotalSaldoBalanceReturnsTwoYears() {
        return saldoTotalAmountRepository.findYearlyTotalSaldoBalance(2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getYear()).isEqualTo("2024");
                    assertThat(result.get(1).getYear()).isEqualTo("2023");
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTotalSaldoBalanceReturnsZeroWhenNoData() {
        return saldoTotalAmountRepository.findYearlyTotalSaldoBalance(2024L)
                .invoke(result -> {
                    assertThat(result).allSatisfy(year -> assertThat(year.getTotalBalance()).isZero());
                })
                .replaceWithVoid();
    }
}
package com.sanedge.transaction.repository.stats;

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
class TransactionStatusRepositoryTest {

        @Inject
        TransactionStatusRepository transactionStatusRepository;

        @Test
        @WithSession
        Uni<Void> testFindMonthTransactionStatusSuccessReturnsTwelveMonths() {
                return transactionStatusRepository
                                .findMonthTransactionStatusSuccess(2024L, 1, 2024L, 12)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(12);
                                        assertThat(result.get(0).getMonth()).isEqualTo("Jan");
                                        assertThat(result.get(11).getMonth()).isEqualTo("Dec");
                                        assertThat(result).allSatisfy(month -> {
                                                assertThat(month.getTotalSuccess()).isZero();
                                                assertThat(month.getTotalAmount()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindMonthTransactionStatusFailedReturnsTwelveMonths() {
                return transactionStatusRepository
                                .findMonthTransactionStatusFailed(2024L, 1, 2024L, 12)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(12);
                                        assertThat(result.get(0).getMonth()).isEqualTo("Jan");
                                        assertThat(result).allSatisfy(month -> {
                                                assertThat(month.getTotalFailed()).isZero();
                                                assertThat(month.getTotalAmount()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransactionStatusSuccessReturnsTwoYearsOrZeroData() {
                return transactionStatusRepository
                                .findYearlyTransactionStatusSuccess(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).anySatisfy(row -> {
                                                assertThat(row.getYear()).isIn("2024", "2023");
                                                assertThat(row.getTotalSuccess()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransactionStatusFailedReturnsTwoYearsOrZeroData() {
                return transactionStatusRepository
                                .findYearlyTransactionStatusFailed(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).anySatisfy(row -> {
                                                assertThat(row.getYear()).isIn("2024", "2023");
                                                assertThat(row.getTotalFailed()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }
}
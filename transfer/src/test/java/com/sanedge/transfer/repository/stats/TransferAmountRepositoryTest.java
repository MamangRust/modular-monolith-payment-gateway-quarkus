package com.sanedge.transfer.repository.stats;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class TransferAmountRepositoryTest {

        @Inject
        TransferAmountRepository transferAmountRepository;

        @Test
        @WithSession
        Uni<Void> testFindMonthlyTransferAmounts_ReturnsTwelveMonths() {
                return transferAmountRepository
                                .findMonthlyTransferAmounts(2024L)
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
        Uni<Void> testFindMonthlyTransferAmounts_ReturnsZeroAmountWhenNoData() {
                return transferAmountRepository
                                .findMonthlyTransferAmounts(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).allSatisfy(month -> {
                                                assertThat(month.getTotalAmount()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindMonthlyTransferAmounts_ReturnsNonNullResult() {
                return transferAmountRepository
                                .findMonthlyTransferAmounts(2023L)
                                .invoke(result -> assertThat(result).isNotNull())
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransferAmounts_ReturnsFiveYears() {
                return transferAmountRepository
                                .findYearlyTransferAmounts(2024L)
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
        Uni<Void> testFindYearlyTransferAmounts_ReturnsZeroAmountWhenNoData() {
                return transferAmountRepository
                                .findYearlyTransferAmounts(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).allSatisfy(year -> {
                                                assertThat(year.getTotalAmount()).isZero();
                                        });
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransferAmounts_ReturnsNonNullResult() {
                return transferAmountRepository
                                .findYearlyTransferAmounts(2023L)
                                .invoke(result -> assertThat(result).isNotNull())
                                .replaceWithVoid();
        }
}
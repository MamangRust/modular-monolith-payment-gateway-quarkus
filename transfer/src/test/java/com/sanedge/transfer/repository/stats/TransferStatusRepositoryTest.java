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
class TransferStatusRepositoryTest {

        @Inject
        TransferStatusRepository transferStatusRepository;

        @Test
        @WithSession
        Uni<Void> testFindMonthTransferStatusSuccess_ReturnsTwoPlaceholders() {
                return transferStatusRepository
                                .findMonthTransferStatusSuccess(2023L, 12, 2023L, 12)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(1);
                                        assertThat(result.get(0).getTotalSuccess()).isZero();
                                        assertThat(result.get(0).getTotalAmount()).isZero();
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransferStatusSuccess_ReturnsTwoPlaceholders() {
                return transferStatusRepository
                                .findYearlyTransferStatusSuccess(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(2);
                                        assertThat(result.get(0).getTotalSuccess()).isZero();
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindMonthTransferStatusFailed_ReturnsTwoPlaceholders() {
                return transferStatusRepository
                                .findMonthTransferStatusFailed(2023L, 12, 2023L, 12)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(1);
                                        assertThat(result.get(0).getTotalFailed()).isZero();
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindYearlyTransferStatusFailed_ReturnsTwoPlaceholders() {
                return transferStatusRepository
                                .findYearlyTransferStatusFailed(2024L)
                                .invoke(result -> {
                                        assertThat(result).isNotNull();
                                        assertThat(result).hasSize(2);
                                        assertThat(result.get(0).getTotalFailed()).isZero();
                                })
                                .replaceWithVoid();
        }

        @Test
        @WithSession
        Uni<Void> testFindMonthTransferStatusSuccess_ReturnsNonNullResult() {
                return transferStatusRepository
                                .findMonthTransferStatusSuccess(2024L, 6, 2024L, 6)
                                .invoke(result -> assertThat(result).isNotNull())
                                .replaceWithVoid();
        }
}
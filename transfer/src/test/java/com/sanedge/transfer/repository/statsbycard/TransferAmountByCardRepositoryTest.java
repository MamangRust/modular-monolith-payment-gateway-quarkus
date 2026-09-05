package com.sanedge.transfer.repository.statsbycard;

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
class TransferAmountByCardRepositoryTest {

    @Inject
    TransferAmountByCardRepository transferAmountByCardRepository;

    private static final String TEST_CARD_NUMBER = "1234567890123456";

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTransferAmountsBySender_ReturnsTwelveMonths() {
        return transferAmountByCardRepository
                .findMonthlyTransferAmountsBySender(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindMonthlyTransferAmountsBySender_ReturnsZeroAmountWhenNoData() {
        return transferAmountByCardRepository
                .findMonthlyTransferAmountsBySender(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindMonthlyTransferAmountsBySender_ReturnsNonNullResult() {
        return transferAmountByCardRepository
                .findMonthlyTransferAmountsBySender("9999999999999999", 2023L)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTransferAmountsBySender_ReturnsFiveYears() {
        return transferAmountByCardRepository
                .findYearlyTransferAmountsBySender(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindYearlyTransferAmountsBySender_ReturnsZeroAmountWhenNoData() {
        return transferAmountByCardRepository
                .findYearlyTransferAmountsBySender(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindMonthlyTransferAmountsByReceiver_ReturnsTwelveMonths() {
        return transferAmountByCardRepository
                .findMonthlyTransferAmountsByReceiver(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindYearlyTransferAmountsByReceiver_ReturnsFiveYears() {
        return transferAmountByCardRepository
                .findYearlyTransferAmountsByReceiver(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindYearlyTransferAmountsByReceiver_ReturnsNonNullResult() {
        return transferAmountByCardRepository
                .findYearlyTransferAmountsByReceiver("9999999999999999", 2023L)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }
}
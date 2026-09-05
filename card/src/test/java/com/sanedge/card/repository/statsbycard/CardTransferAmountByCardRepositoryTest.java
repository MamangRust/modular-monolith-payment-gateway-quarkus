package com.sanedge.card.repository.statsbycard;

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
class CardTransferAmountByCardRepositoryTest {

    @Inject
    CardTransferAmountByCardRepository repository;

    private final String CARD = "1234-5678-9012-3456";

    @Test
    @WithSession
    Uni<Void> testGetMonthlyTransferAmountBySender_ReturnsTwelveMonthsWithZero() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        return repository.getMonthlyTransferAmountBySender(CARD, date)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(0).getMonth()).isEqualTo("Jan");
                    assertThat(result).allSatisfy(m -> assertThat(m.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetMonthlyTransferAmountByReceiver_ReturnsTwelveMonthsWithZero() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        return repository.getMonthlyTransferAmountByReceiver(CARD, date)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(11).getMonth()).isEqualTo("Dec");
                    assertThat(result).allSatisfy(m -> assertThat(m.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetYearlyTransferAmountBySender_ReturnsFiveYearsWithZero() {
        return repository.getYearlyTransferAmountBySender(CARD, 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(5);
                    assertThat(result.get(0).getYear()).isEqualTo("2020");
                    assertThat(result.get(4).getYear()).isEqualTo("2024");
                    assertThat(result).allSatisfy(y -> assertThat(y.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testGetYearlyTransferAmountByReceiver_ReturnsFiveYearsWithZero() {
        return repository.getYearlyTransferAmountByReceiver(CARD, 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(5);
                    assertThat(result.get(0).getYear()).isEqualTo("2020");
                    assertThat(result).allSatisfy(y -> assertThat(y.getTotalAmount()).isZero());
                })
                .replaceWithVoid();
    }
}
package com.sanedge.transaction.repository.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sanedge.transaction.domain.requests.statsbycard.MonthStatusTransactionCardNumber;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class TransactionStatusByCardRepositoryTest {

    @Inject
    TransactionStatusByCardRepository repository;

    private MonthStatusTransactionCardNumber monthReq(String cardNumber, Long year) {
        MonthStatusTransactionCardNumber req = new MonthStatusTransactionCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear(year);
        return req;
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthTransactionStatusSuccessByCardReturnsTwelveMonths() {
        return repository.findMonthTransactionStatusSuccessByCard(monthReq("CARD-1", 2024L))
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
    Uni<Void> testFindMonthTransactionStatusFailedByCardReturnsTwelveMonths() {
        return repository.findMonthTransactionStatusFailedByCard(monthReq("CARD-1", 2024L))
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
    Uni<Void> testFindYearlyTransactionStatusSuccessByCardReturnsTwoYearsOrZeroData() {
        return repository.findYearlyTransactionStatusSuccessByCard("CARD-1", 2024L)
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
    Uni<Void> testFindYearlyTransactionStatusFailedByCardReturnsTwoYearsOrZeroData() {
        return repository.findYearlyTransactionStatusFailedByCard("CARD-1", 2024L)
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
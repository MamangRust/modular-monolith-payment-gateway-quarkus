package com.sanedge.topup.repository.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sanedge.topup.domain.requests.statsbycard.MonthTopupStatusCardNumber;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class TopupStatusByCardRepositoryTest {

    @Inject
    TopupStatusByCardRepository repository;

    private MonthTopupStatusCardNumber monthReq(String cardNumber, Long year) {
        MonthTopupStatusCardNumber req = new MonthTopupStatusCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear(year);
        return req;
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthTopupStatusSuccessByCardReturnsTwelveMonths() {
        return repository.findMonthTopupStatusSuccessByCard(monthReq("CARD-1", 2024L))
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
    Uni<Void> testFindMonthTopupStatusFailedByCardReturnsTwelveMonths() {
        return repository.findMonthTopupStatusFailedByCard(monthReq("CARD-1", 2024L))
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
    Uni<Void> testFindYearlyTopupStatusSuccessByCardReturnsTwoYearsOrZeroData() {
        return repository.findYearlyTopupStatusSuccessByCard("CARD-1", 2024L)
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
    Uni<Void> testFindYearlyTopupStatusFailedByCardReturnsTwoYearsOrZeroData() {
        return repository.findYearlyTopupStatusFailedByCard("CARD-1", 2024L)
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
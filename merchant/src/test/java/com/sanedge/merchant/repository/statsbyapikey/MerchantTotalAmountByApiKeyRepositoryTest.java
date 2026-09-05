package com.sanedge.merchant.repository.statsbyapikey;

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
class MerchantTotalAmountByApiKeyRepositoryTest {

    @Inject
    MerchantTotalAmountByApiKeyRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyTotalAmountByApiKeyReturnsZeroAmountsWhenNoData() {
        LocalDate date = LocalDate.of(2024, 6, 15);
        return repository.findMonthlyTotalAmountByApiKey("non-existent-key", date)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).anySatisfy(item -> {
                        assertThat(item.getMonth()).isIn(
                                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
                        assertThat(item.getTotalAmount()).isZero();
                    });
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTotalAmountByApiKeyReturnsTwoYearsWithZeroWhenNoData() {
        return repository.findYearlyTotalAmountByApiKey("non-existent-key", 2024L, 2023L)
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
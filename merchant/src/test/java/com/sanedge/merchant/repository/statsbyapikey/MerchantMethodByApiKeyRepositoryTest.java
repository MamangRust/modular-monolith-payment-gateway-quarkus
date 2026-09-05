package com.sanedge.merchant.repository.statsbyapikey;

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
class MerchantMethodByApiKeyRepositoryTest {

    @Inject
    MerchantMethodByApiKeyRepository repository;

    @Test
    @WithSession
    Uni<Void> testFindMonthlyPaymentMethodsByApiKeyReturnsEmptyWhenNoData() {
        return repository.findMonthlyPaymentMethodsByApiKey("non-existent-key", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyPaymentMethodsByApiKeyReturnsEmptyWhenNoData() {
        return repository.findYearlyPaymentMethodsByApiKey("non-existent-key", 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).isEmpty();
                })
                .replaceWithVoid();
    }
}
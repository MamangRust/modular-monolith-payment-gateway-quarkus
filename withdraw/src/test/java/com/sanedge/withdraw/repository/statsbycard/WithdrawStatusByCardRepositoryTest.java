package com.sanedge.withdraw.repository.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sanedge.withdraw.domain.requests.statsbycard.MonthStatusWithdrawCardNumber;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class WithdrawStatusByCardRepositoryTest {

    @Inject
    WithdrawStatusByCardRepository withdrawStatusByCardRepository;

    private static final String TEST_CARD_NUMBER = "1234567890123456";

    private MonthStatusWithdrawCardNumber buildRequest(String cardNumber, int year, int month) {
        MonthStatusWithdrawCardNumber req = new MonthStatusWithdrawCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear(year);
        req.setMonth(month);
        return req;
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthWithdrawStatusSuccessByCard_ReturnsOneRow() {
        MonthStatusWithdrawCardNumber req = buildRequest(TEST_CARD_NUMBER, 2024, 6);
        return withdrawStatusByCardRepository
                .findMonthWithdrawStatusSuccessByCard(req)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1);
                    assertThat(result.get(0).getMonth()).isEqualTo("Jun");
                    assertThat(result.get(0).getTotalSuccess()).isZero();
                    assertThat(result.get(0).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthWithdrawStatusSuccessByCard_ReturnsNonNullResult() {
        MonthStatusWithdrawCardNumber req = buildRequest("9999999999999999", 2024, 12);
        return withdrawStatusByCardRepository
                .findMonthWithdrawStatusSuccessByCard(req)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthWithdrawStatusFailedByCard_ReturnsOneRow() {
        MonthStatusWithdrawCardNumber req = buildRequest(TEST_CARD_NUMBER, 2024, 6);
        return withdrawStatusByCardRepository
                .findMonthWithdrawStatusFailedByCard(req)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(1);
                    assertThat(result.get(0).getMonth()).isEqualTo("Jun");
                    assertThat(result.get(0).getTotalFailed()).isZero();
                    assertThat(result.get(0).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthWithdrawStatusFailedByCard_ReturnsNonNullResult() {
        MonthStatusWithdrawCardNumber req = buildRequest("9999999999999999", 2024, 12);
        return withdrawStatusByCardRepository
                .findMonthWithdrawStatusFailedByCard(req)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyWithdrawStatusSuccessByCard_ReturnsTwoPlaceholders() {
        return withdrawStatusByCardRepository
                .findYearlyWithdrawStatusSuccessByCard(TEST_CARD_NUMBER, 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getTotalSuccess()).isZero();
                    assertThat(result.get(0).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyWithdrawStatusSuccessByCard_ReturnsNonNullResult() {
        return withdrawStatusByCardRepository
                .findYearlyWithdrawStatusSuccessByCard("9999999999999999", 2023L)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyWithdrawStatusFailedByCard_ReturnsTwoPlaceholders() {
        return withdrawStatusByCardRepository
                .findYearlyWithdrawStatusFailedByCard(TEST_CARD_NUMBER, 2024L)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(2);
                    assertThat(result.get(0).getTotalFailed()).isZero();
                    assertThat(result.get(0).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyWithdrawStatusFailedByCard_ReturnsNonNullResult() {
        return withdrawStatusByCardRepository
                .findYearlyWithdrawStatusFailedByCard("9999999999999999", 2023L)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }
}
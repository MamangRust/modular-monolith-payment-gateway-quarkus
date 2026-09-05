package com.sanedge.transfer.repository.statsbycard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sanedge.transfer.domain.requests.statsbycard.MonthStatusTransferCardNumber;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@Disabled("Stats SQL uses PostgreSQL-specific functions; enable after verifying DB compatibility")
@QuarkusTest
@RunOnVertxContext
class TransferStatusByCardRepositoryTest {

    @Inject
    TransferStatusByCardRepository transferStatusByCardRepository;

    private static final String TEST_CARD_NUMBER = "1234567890123456";

    private MonthStatusTransferCardNumber buildMonthReq(String cardNumber, int year, int month) {
        MonthStatusTransferCardNumber req = new MonthStatusTransferCardNumber();
        req.setCardNumber(cardNumber);
        req.setYear(year);
        req.setMonth(month);
        return req;
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthTransferStatusSuccessByCard_ReturnsOneRow() {
        MonthStatusTransferCardNumber req = buildMonthReq(TEST_CARD_NUMBER, 2024, 6);
        return transferStatusByCardRepository
                .findMonthTransferStatusSuccessByCard(req)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(5).getMonth()).isEqualTo("Jun");
                    assertThat(result.get(5).getTotalSuccess()).isZero();
                    assertThat(result.get(5).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindMonthTransferStatusSuccessByCard_ReturnsNonNullResult() {
        MonthStatusTransferCardNumber req = buildMonthReq("9999999999999999", 2024, 12);
        return transferStatusByCardRepository
                .findMonthTransferStatusSuccessByCard(req)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTransferStatusSuccessByCard_ReturnsTwoPlaceholders() {
        return transferStatusByCardRepository
                .findYearlyTransferStatusSuccessByCard(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindMonthTransferStatusFailedByCard_ReturnsOneRow() {
        MonthStatusTransferCardNumber req = buildMonthReq(TEST_CARD_NUMBER, 2024, 6);
        return transferStatusByCardRepository
                .findMonthTransferStatusFailedByCard(req)
                .invoke(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result).hasSize(12);
                    assertThat(result.get(5).getMonth()).isEqualTo("Jun");
                    assertThat(result.get(5).getTotalFailed()).isZero();
                    assertThat(result.get(5).getTotalAmount()).isZero();
                })
                .replaceWithVoid();
    }

    @Test
    @WithSession
    Uni<Void> testFindYearlyTransferStatusFailedByCard_ReturnsTwoPlaceholders() {
        return transferStatusByCardRepository
                .findYearlyTransferStatusFailedByCard(TEST_CARD_NUMBER, 2024L)
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
    Uni<Void> testFindYearlyTransferStatusFailedByCard_ReturnsNonNullResult() {
        return transferStatusByCardRepository
                .findYearlyTransferStatusFailedByCard("9999999999999999", 2023L)
                .invoke(result -> assertThat(result).isNotNull())
                .replaceWithVoid();
    }
}
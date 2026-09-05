package com.sanedge.merchant.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactions;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsById;
import com.sanedge.merchant.domain.requests.transactions.FindAllMerchantTransactionsByApiKey;
import com.sanedge.merchant.entity.Merchant;
import com.sanedge.merchant.entity.Transaction;
import com.sanedge.common.enums.Status;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@QuarkusTest
@RunOnVertxContext
class MerchantTransactionRepositoryTest {

    @Inject
    MerchantTransactionRepository merchantTransactionRepo;

    private Uni<Merchant> createMerchant(String name, String apiKey, Status status) {
        Merchant m = new Merchant();
        m.setName(name);
        m.setApiKey(apiKey);
        m.setUserId(1);
        m.setStatus(status);
        m.setMerchantNo(java.util.UUID.randomUUID());
        m.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        m.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return m.persist().map(v -> m);
    }

    private Uni<Merchant> createMerchant(String name, String apiKey) {
        return createMerchant(name, apiKey, Status.SUCCESS);
    }

    private Uni<Transaction> createTransaction(Long merchantId, String cardNumber,
            int amount, String paymentMethod,
            LocalDateTime transactionTime) {
        Transaction t = new Transaction();
        t.setMerchantId(merchantId.intValue()); // entity uses Integer
        t.setCardNumber(cardNumber);
        t.setAmount(amount);
        t.setPaymentMethod(paymentMethod);
        t.setTransactionTime(Timestamp.valueOf(transactionTime));
        t.setTransactionNo(java.util.UUID.randomUUID());
        t.setStatus(Status.SUCCESS);
        t.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        t.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return t.persist().map(v -> t);
    }

    private Uni<Void> cleanAll() {
        return Transaction.deleteAll()
                .chain(() -> Merchant.deleteAll())
                .replaceWithVoid();
    }

    // ---------- findAllTransactions ----------

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsPagination() {
        return cleanAll()
                .chain(() -> createMerchant("Shop1", "key1"))
                .chain(m -> createTransaction(m.getMerchantId(), "CARD-1", 1000, "CASH",
                        LocalDateTime.of(2026, 7, 1, 10, 0)))
                .chain(() -> createTransaction(1L, "CARD-2", 2000, "CREDIT",
                        LocalDateTime.of(2026, 7, 1, 11, 0))) // keep same merchant
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(1);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(page1 -> {
                    assertThat(page1.getData()).hasSize(1);
                    assertThat(page1.getTotalRecords()).isEqualTo(2);
                    // sorted by transaction_time DESC, so latest first: CARD-2
                    assertThat(page1.getData().get(0).getCardNumber()).isEqualTo("CARD-2");
                })
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(2);
                    req.setPageSize(1);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(page2 -> {
                    assertThat(page2.getData()).hasSize(1);
                    assertThat(page2.getData().get(0).getCardNumber()).isEqualTo("CARD-1");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsSearchByCardNumber() {
        return cleanAll()
                .chain(() -> createMerchant("M1", "k1"))
                .chain(m -> createTransaction(m.getMerchantId(), "XYZ-111", 500, "CASH",
                        LocalDateTime.now()))
                .chain(() -> createTransaction(1L, "XYZ-222", 600, "CASH",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("111");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getCardNumber()).isEqualTo("XYZ-111");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsSearchByPaymentMethod() {
        return cleanAll()
                .chain(() -> createMerchant("M2", "k2"))
                .chain(m -> createTransaction(m.getMerchantId(), "C1", 10, "QRIS",
                        LocalDateTime.now()))
                .chain(() -> createTransaction(1L, "C2", 20, "CREDIT",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("qris");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getPaymentMethod()).isEqualTo("QRIS");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsEmptySearchReturnsAll() {
        return cleanAll()
                .chain(() -> createMerchant("M3", "k3"))
                .chain(m -> createTransaction(m.getMerchantId(), "A", 1, "CASH",
                        LocalDateTime.now()))
                .chain(() -> createTransaction(1L, "B", 2, "CASH",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isEqualTo(2))
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsNullSearchReturnsAll() {
        return cleanAll()
                .chain(() -> createMerchant("M4", "k4"))
                .chain(m -> createTransaction(m.getMerchantId(), "C", 1, "DEBIT",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch(null);
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isEqualTo(1))
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsSorting() {
        LocalDateTime t1 = LocalDateTime.of(2026, 7, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 7, 1, 12, 0);
        LocalDateTime t3 = LocalDateTime.of(2026, 7, 1, 11, 0);

        return cleanAll()
                .chain(() -> createMerchant("S", "sk"))
                .chain(m -> createTransaction(m.getMerchantId(), "C1", 100, "CASH", t1)
                        .chain(() -> createTransaction(m.getMerchantId(), "C2", 200, "CASH", t2))
                        .chain(() -> createTransaction(m.getMerchantId(), "C3", 300, "CASH", t3)))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> {
                    assertThat(r.getData()).hasSize(3);
                    // descending order by transaction_time
                    assertThat(r.getData().get(0).getTransactionTime()).isEqualTo(Timestamp.valueOf(t2));
                    assertThat(r.getData().get(1).getTransactionTime()).isEqualTo(Timestamp.valueOf(t3));
                    assertThat(r.getData().get(2).getTransactionTime()).isEqualTo(Timestamp.valueOf(t1));
                })
                .replaceWithVoid();
    }

    // ---------- findAllTransactionsByMerchant ----------

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByMerchantBasic() {
        return cleanAll()
                .chain(() -> createMerchant("MerchA", "keyA"))
                .chain(mA -> createTransaction(mA.getMerchantId(), "XA", 500, "CASH",
                        LocalDateTime.of(2026, 7, 1, 9, 0)))
                .chain(() -> createMerchant("MerchB", "keyB"))
                .chain(mB -> {
                    FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                    req.setMerchantId(mB.getMerchantId());
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactionsByMerchant(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isEqualTo(0)) // no tx for B
                .chain(() -> {
                    FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                    req.setMerchantId(1L); // first merchant
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactionsByMerchant(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getCardNumber()).isEqualTo("XA");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByMerchantWithSearch() {
        return cleanAll()
                .chain(() -> createMerchant("MS", "ks"))
                .chain(m -> createTransaction(m.getMerchantId(), "FIND-1", 10, "CASH",
                        LocalDateTime.now())
                        .chain(() -> createTransaction(m.getMerchantId(), "IGNORE-2", 20, "CASH",
                                LocalDateTime.now())))
                .chain(() -> {
                    FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                    req.setMerchantId(1L);
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("FIND");
                    return merchantTransactionRepo.findAllTransactionsByMerchant(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getCardNumber()).isEqualTo("FIND-1");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByMerchantPagination() {
        return cleanAll()
                .chain(() -> createMerchant("MP", "kp"))
                .chain(m -> {
                    Uni<Void> chain = Uni.createFrom().voidItem();
                    for (int i = 1; i <= 3; i++) {
                        int num = i;
                        chain = chain.chain(() -> createTransaction(m.getMerchantId(),
                                "C" + num, num * 100, "CASH",
                                LocalDateTime.now()).replaceWithVoid());
                    }
                    return chain;
                })
                .chain(() -> {
                    FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                    req.setMerchantId(1L);
                    req.setPage(1);
                    req.setPageSize(2);
                    return merchantTransactionRepo.findAllTransactionsByMerchant(req);
                })
                .invoke(page1 -> {
                    assertThat(page1.getData()).hasSize(2);
                    assertThat(page1.getTotalRecords()).isEqualTo(3);
                })
                .chain(() -> {
                    FindAllMerchantTransactionsById req = new FindAllMerchantTransactionsById();
                    req.setMerchantId(1L);
                    req.setPage(2);
                    req.setPageSize(2);
                    return merchantTransactionRepo.findAllTransactionsByMerchant(req);
                })
                .invoke(page2 -> assertThat(page2.getData()).hasSize(1))
                .replaceWithVoid();
    }

    // ---------- findAllTransactionsByApiKey ----------

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByApiKeyBasic() {
        return cleanAll()
                .chain(() -> createMerchant("ApiShop", "secret-123"))
                .chain(m -> createTransaction(m.getMerchantId(), "API-1", 999, "QRIS",
                        LocalDateTime.of(2026, 7, 5, 12, 0)))
                .chain(() -> createMerchant("OtherShop", "other-key"))
                .chain(() -> {
                    FindAllMerchantTransactionsByApiKey req = new FindAllMerchantTransactionsByApiKey();
                    req.setApiKey("secret-123");
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactionsByApiKey(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getAmount()).isEqualTo(999);
                    assertThat(r.getData().get(0).getMerchantName()).isEqualTo("ApiShop");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByApiKeyWithSearch() {
        return cleanAll()
                .chain(() -> createMerchant("KeyShop", "apikey-X"))
                .chain(m -> createTransaction(m.getMerchantId(), "KEY-1", 111, "DEBIT",
                        LocalDateTime.now())
                        .chain(() -> createTransaction(m.getMerchantId(), "OTHER-2", 222, "CREDIT",
                                LocalDateTime.now())))
                .chain(() -> {
                    FindAllMerchantTransactionsByApiKey req = new FindAllMerchantTransactionsByApiKey();
                    req.setApiKey("apikey-X");
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("DEBIT");
                    return merchantTransactionRepo.findAllTransactionsByApiKey(req);
                })
                .invoke(r -> {
                    assertThat(r.getTotalRecords()).isEqualTo(1);
                    assertThat(r.getData().get(0).getPaymentMethod()).isEqualTo("DEBIT");
                })
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testFindAllTransactionsByApiKeyNotFoundApiKey() {
        return cleanAll()
                .chain(() -> createMerchant("Real", "real"))
                .chain(() -> {
                    FindAllMerchantTransactionsByApiKey req = new FindAllMerchantTransactionsByApiKey();
                    req.setApiKey("nonexistent");
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactionsByApiKey(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isZero())
                .replaceWithVoid();
    }

    // ---------- edge cases ----------

    @Test
    @WithTransaction
    Uni<Void> testEmptyDatabaseReturnsZeroRecords() {
        return cleanAll()
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isZero())
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testPageZeroDefaultsToFirstPage() {
        return cleanAll()
                .chain(() -> createMerchant("P0", "k0"))
                .chain(m -> createTransaction(m.getMerchantId(), "Z", 1, "CASH",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(0);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isEqualTo(1))
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testNegativePageDefaultsToFirstPage() {
        return cleanAll()
                .chain(() -> createMerchant("NEG", "nk"))
                .chain(m -> createTransaction(m.getMerchantId(), "N", 5, "CASH",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(-1);
                    req.setPageSize(10);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getTotalRecords()).isEqualTo(1))
                .replaceWithVoid();
    }

    @Test
    @WithTransaction
    Uni<Void> testZeroSizeDefaultsToTen() {
        return cleanAll()
                .chain(() -> createMerchant("ZS", "zs"))
                .chain(m -> createTransaction(m.getMerchantId(), "Z2", 10, "CASH",
                        LocalDateTime.now()))
                .chain(() -> {
                    FindAllMerchantTransactions req = new FindAllMerchantTransactions();
                    req.setPage(1);
                    req.setPageSize(0);
                    req.setSearch("");
                    return merchantTransactionRepo.findAllTransactions(req);
                })
                .invoke(r -> assertThat(r.getData()).hasSizeLessThanOrEqualTo(10))
                .replaceWithVoid();
    }
}
package com.sanedge.transaction.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sanedge.common.config.RedisService;
import com.sanedge.common.domain.response.ApiResponse;
import com.sanedge.common.enums.Status;
import com.sanedge.common.observability.TracingMetrics;
import com.sanedge.transaction.domain.requests.CreateTransactionRequest;
import com.sanedge.transaction.domain.requests.UpdateTransactionRequest;
import com.sanedge.transaction.domain.response.TransactionResponse;
import com.sanedge.transaction.domain.response.TransactionResponseDeleteAt;
import com.sanedge.transaction.entity.Transaction;
import com.sanedge.transaction.repository.TransactionCommandRepository;
import com.sanedge.transaction.repository.TransactionQueryRepository;
import com.sanedge.transaction.service.KafkaService;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.validation.Validator;
import pb.card.Card;
import pb.card.CardQueryService;
import pb.merchant.Merchant;
import pb.merchant.MerchantQueryService;
import pb.saldo.Saldo;
import pb.saldo.SaldoCommandService;
import pb.saldo.SaldoQueryService;

/**
 * Unit tests for {@link TransactionCommandServiceImpl}.
 * <p>
 * Covers all CRUD operations: create, update, trashed, restore,
 * deletePermanent, restoreAll, deleteAll.
 */
@ExtendWith(MockitoExtension.class)
class TransactionCommandServiceImplTest {

    @Mock
    CardQueryService cardQueryService;

    @Mock
    MerchantQueryService merchantQueryService;

    @Mock
    SaldoQueryService saldoQueryService;

    @Mock
    SaldoCommandService saldoCommandService;

    @Mock
    TransactionQueryRepository transactionQueryRepository;

    @Mock
    TransactionCommandRepository transactionCommandRepository;

    @Mock
    Validator validator;

    @Mock
    RedisService redisService;

    @Mock
    KafkaService kafkaService;

    @Mock
    TracingMetrics tracingMetrics;

    @InjectMocks
    TransactionCommandServiceImpl transactionCommandService;

    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.transactionId = 1L;
        transaction.transactionNo = UUID.randomUUID();
        transaction.cardNumber = "4111111111111111";
        transaction.amount = 150000;
        transaction.paymentMethod = "CREDIT_CARD";
        transaction.merchantId = 1;
        transaction.transactionTime = Timestamp.valueOf(LocalDateTime.now());
        transaction.status = Status.PENDING;

        // TracingMetrics no-op mock
        lenient().when(tracingMetrics.traceAndMeasure(anyString(), anyString(), any(), any()))
                .thenAnswer(inv -> {
                    java.util.function.Supplier<Uni<?>> supplier = inv.getArgument(3);
                    return supplier.get();
                });
        lenient().when(tracingMetrics.traceAndMeasure(anyString(), anyString(), any()))
                .thenAnswer(inv -> {
                    java.util.function.Supplier<Uni<?>> supplier = inv.getArgument(2);
                    return supplier.get();
                });

        // Validator passes by default
        lenient().when(validator.validate(any())).thenReturn(Set.of());

        // Redis cache-miss by default
        lenient().when(redisService.getReactive(anyString())).thenReturn(Uni.createFrom().nullItem());
        lenient().when(redisService.setWithExpirationReactive(anyString(), anyString(), any(Long.class)))
                .thenReturn(Uni.createFrom().voidItem());
        lenient().when(redisService.deleteReactive(anyString())).thenReturn(Uni.createFrom().voidItem());
    }

    // ──────────────────────────────────────────────
    // Create Transaction
    // ──────────────────────────────────────────────

    @Test
    void createTransaction_success() {
        Merchant.MerchantResponse merchantResponse = Merchant.MerchantResponse.newBuilder()
                .setId(1)
                .setUserId(1)
                .build();
        Merchant.ApiResponseMerchant merchantApiResponse = Merchant.ApiResponseMerchant.newBuilder()
                .setStatus("success")
                .setData(merchantResponse)
                .build();
        when(merchantQueryService.findByApiKey(any()))
                .thenReturn(Uni.createFrom().item(merchantApiResponse));

        Card.CardWithEmailResponse cardWithEmail = Card.CardWithEmailResponse.newBuilder()
                .setCardNumber("4111111111111111")
                .setEmail("test@example.com")
                .build();
        when(cardQueryService.findUserCardByCardNumber(any()))
                .thenReturn(Uni.createFrom().item(cardWithEmail));

        Saldo.SaldoResponse saldoResponse = Saldo.SaldoResponse.newBuilder()
                .setSaldoId(1)
                .setCardNumber("4111111111111111")
                .setTotalBalance(500000)
                .build();
        Saldo.ApiResponseSaldo apiSaldo = Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success")
                .setData(saldoResponse)
                .build();
        when(saldoQueryService.findByCardNumber(any()))
                .thenReturn(Uni.createFrom().item(apiSaldo));

        Saldo.ApiResponseSaldo updateSaldoResp = Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success")
                .build();
        when(saldoCommandService.updateSaldoBalance(any()))
                .thenReturn(Uni.createFrom().item(updateSaldoResp));

        when(transactionCommandRepository.persist(any(com.sanedge.transaction.entity.Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    t.transactionId = 1L;
                    return Uni.createFrom().item(t);
                });

        when(transactionCommandRepository.updateTransactionStatus(any(Long.class), anyString()))
                .thenAnswer(inv -> {
                    transaction.transactionId = 1L;
                    transaction.status = Status.SUCCESS;
                    return Uni.createFrom().item(transaction);
                });

        Card.CardResponse merchantCard = Card.CardResponse.newBuilder()
                .setCardNumber("2222222222222222")
                .build();
        Card.ApiResponseCard merchantCardApiResp = Card.ApiResponseCard.newBuilder()
                .setStatus("success")
                .setData(merchantCard)
                .build();
        when(cardQueryService.findByUserIdCard(any()))
                .thenReturn(Uni.createFrom().item(merchantCardApiResp));

        when(kafkaService.sendMessage(anyString(), anyString(), any(JsonObject.class)))
                .thenReturn(Uni.createFrom().voidItem());

        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setCardNumber("4111111111111111");
        req.setAmount(150000L);
        req.setPaymentMethod("CREDIT_CARD");
        req.setMerchantId(1L);

        ApiResponse<TransactionResponse> result = transactionCommandService.create("test-api-key", req)
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getCardNumber()).isEqualTo("4111111111111111");
        // Two-phase ledger: PENDING reservation persist, then final persist with leg-B metadata
        verify(transactionCommandRepository, times(2)).persist(any(com.sanedge.transaction.entity.Transaction.class));
    }

    // ──────────────────────────────────────────────
    // Update Transaction
    // ──────────────────────────────────────────────

    @Test
    void updateTransaction_success() {
        Transaction existing = new Transaction();
        existing.transactionId = 1L;
        existing.cardNumber = "4111111111111111";
        existing.amount = 50000;
        existing.paymentMethod = "CREDIT_CARD";
        existing.merchantId = 1;
        existing.status = Status.SUCCESS;
        when(transactionQueryRepository.findTransactionById(1L))
                .thenReturn(Uni.createFrom().item(existing));

        Merchant.MerchantResponse merchantResponse = Merchant.MerchantResponse.newBuilder()
                .setId(1)
                .setUserId(1)
                .build();
        Merchant.ApiResponseMerchant merchantApiResponse = Merchant.ApiResponseMerchant.newBuilder()
                .setStatus("success")
                .setData(merchantResponse)
                .build();
        when(merchantQueryService.findByApiKey(any()))
                .thenReturn(Uni.createFrom().item(merchantApiResponse));

        Card.CardResponse cardResponseData = Card.CardResponse.newBuilder()
                .setCardNumber("4111111111111111")
                .build();
        Card.ApiResponseCard cardApiResponse = Card.ApiResponseCard.newBuilder()
                .setStatus("success")
                .setData(cardResponseData)
                .build();
        when(cardQueryService.findByCardNumber(any()))
                .thenReturn(Uni.createFrom().item(cardApiResponse));

        Saldo.SaldoResponse saldoResponse = Saldo.SaldoResponse.newBuilder()
                .setSaldoId(1)
                .setCardNumber("4111111111111111")
                .setTotalBalance(500000)
                .build();
        Saldo.ApiResponseSaldo apiSaldo = Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success")
                .setData(saldoResponse)
                .build();
        when(saldoQueryService.findByCardNumber(any()))
                .thenReturn(Uni.createFrom().item(apiSaldo));

        Saldo.ApiResponseSaldo updateSaldoResp = Saldo.ApiResponseSaldo.newBuilder()
                .setStatus("success")
                .build();
        when(saldoCommandService.updateSaldoBalance(any()))
                .thenReturn(Uni.createFrom().item(updateSaldoResp));

        when(transactionCommandRepository.persist(any(com.sanedge.transaction.entity.Transaction.class)))
                .thenAnswer(inv -> {
                    Transaction t = inv.getArgument(0);
                    return Uni.createFrom().item(t);
                });

        when(transactionCommandRepository.updateTransactionStatus(any(Long.class), anyString()))
                .thenAnswer(inv -> {
                    transaction.transactionId = 1L;
                    transaction.status = Status.SUCCESS;
                    return Uni.createFrom().item(transaction);
                });

        when(redisService.deleteReactive(anyString())).thenReturn(Uni.createFrom().voidItem());

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setTransactionId(1L);
        req.setCardNumber("4111111111111111");
        req.setAmount(150000L);
        req.setPaymentMethod("CREDIT_CARD");
        req.setMerchantId(1L);

        ApiResponse<TransactionResponse> result = transactionCommandService.update("test-api-key", req)
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        verify(transactionQueryRepository).findTransactionById(1L);
        verify(transactionCommandRepository).persist(any(com.sanedge.transaction.entity.Transaction.class));
    }

    // ──────────────────────────────────────────────
    // Trash / Restore / Delete
    // ──────────────────────────────────────────────

    @Test
    void trashed_success() {
        when(transactionCommandRepository.trashed(1L)).thenReturn(Uni.createFrom().item(transaction));

        ApiResponse<TransactionResponseDeleteAt> result = transactionCommandService.trashed(1L)
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        verify(transactionCommandRepository).trashed(1L);
    }

    @Test
    void restore_success() {
        when(transactionCommandRepository.restore(1L)).thenReturn(Uni.createFrom().item(transaction));

        ApiResponse<TransactionResponseDeleteAt> result = transactionCommandService.restore(1L)
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        verify(transactionCommandRepository).restore(1L);
    }

    @Test
    void deletePermanent_success() {
        Transaction trashedTx = new Transaction();
        trashedTx.transactionId = 1L;
        trashedTx.cardNumber = "4111111111111111";
        trashedTx.merchantId = 1;
        trashedTx.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        when(transactionCommandRepository.findById(1L)).thenReturn(Uni.createFrom().item(trashedTx));
        when(transactionCommandRepository.deletePermanent(1L)).thenReturn(Uni.createFrom().item(true));

        ApiResponse<Boolean> result = transactionCommandService.deletePermanent(1L)
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.data()).isTrue();
        verify(transactionCommandRepository).deletePermanent(1L);
    }

    @Test
    void restoreAll_success() {
        when(transactionCommandRepository.restoreAllDeleted()).thenReturn(Uni.createFrom().item(true));

        ApiResponse<Boolean> result = transactionCommandService.restoreAll()
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.data()).isTrue();
    }

    @Test
    void deleteAll_success() {
        when(transactionCommandRepository.deleteAllDeleted()).thenReturn(Uni.createFrom().item(true));

        ApiResponse<Boolean> result = transactionCommandService.deleteAll()
                .await().indefinitely();

        assertThat(result.status()).isEqualTo("success");
        assertThat(result.data()).isTrue();
    }
}

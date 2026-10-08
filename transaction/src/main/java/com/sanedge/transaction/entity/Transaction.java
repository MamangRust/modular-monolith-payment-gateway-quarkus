package com.sanedge.transaction.entity;

import com.sanedge.common.entity.BaseModel;

import java.sql.Timestamp;
import java.util.UUID;

import com.sanedge.common.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    public Long transactionId;

    @Column(name = "transaction_no", nullable = false, unique = true)
    public UUID transactionNo;

    @Column(name = "card_number", nullable = false)
    public String cardNumber;

    @Column(nullable = false)
    public Integer amount;

    @Column(name = "payment_method", nullable = false)
    public String paymentMethod;

    @Column(name = "merchant_id", nullable = false)
    public Integer merchantId;

    @Column(name = "transaction_time", nullable = false)
    public Timestamp transactionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.PENDING;

    @Column(name = "compensation_leg_a_applied")
    public Boolean compensationLegAApplied;

    @Column(name = "compensation_leg_a_card")
    public String compensationLegACard;

    @Column(name = "compensation_leg_a_delta")
    public Integer compensationLegADelta;

    @Column(name = "compensation_leg_b_applied")
    public Boolean compensationLegBApplied;

    @Column(name = "compensation_leg_b_card")
    public String compensationLegBCard;

    @Column(name = "compensation_leg_b_delta")
    public Integer compensationLegBDelta;

    @Column(name = "compensation_worker_id")
    public String compensationWorkerId;

    @Column(name = "compensation_claim_token")
    public String compensationClaimToken;

    @Column(name = "compensation_lease_until")
    public Timestamp compensationLeaseUntil;

    @Column(name = "compensation_attempts")
    public Integer compensationAttempts = 0;

    @Column(name = "compensation_reason")
    public String compensationReason;

    public Boolean getCompensationLegAApplied() { return compensationLegAApplied; }
    public String getCompensationLegACard() { return compensationLegACard; }
    public Integer getCompensationLegADelta() { return compensationLegADelta; }
    public Boolean getCompensationLegBApplied() { return compensationLegBApplied; }
    public String getCompensationLegBCard() { return compensationLegBCard; }
    public Integer getCompensationLegBDelta() { return compensationLegBDelta; }
    public Long getTransactionId() { return transactionId; }
}
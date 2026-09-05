package com.sanedge.topup.entity;

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
@Table(name = "topups")
public class Topup extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topup_id")
    public Long topupId;

    @Column(name = "topup_no", nullable = false, unique = true)
    public UUID topupNo;

    @Column(name = "card_number", nullable = false)
    public String cardNumber;

    @Column(name = "topup_amount", nullable = false)
    public Integer topupAmount;

    @Column(name = "topup_method", nullable = false)
    public String topupMethod;

    @Column(name = "topup_time", nullable = false)
    public Timestamp topupTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Status status = Status.PENDING;

    @Column(name = "compensation_leg_a_applied")
    public Boolean compensationLegAApplied;

    @Column(name = "compensation_leg_a_card")
    public String compensationLegACard;

    @Column(name = "compensation_leg_a_delta")
    public Integer compensationLegADelta;

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

    public Boolean getCompensationLegAApplied() {
        return compensationLegAApplied;
    }

    public String getCompensationLegACard() {
        return compensationLegACard;
    }

    public Integer getCompensationLegADelta() {
        return compensationLegADelta;
    }
}

package com.sanedge.transfer.entity;

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
@Table(name = "transfers")
public class Transfer extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    public Long transferId;

    @Column(name = "transfer_no", nullable = false, unique = true)
    public UUID transferNo;

    @Column(name = "transfer_from", nullable = false)
    public String transferFrom;

    @Column(name = "transfer_to", nullable = false)
    public String transferTo;

    @Column(name = "transfer_amount", nullable = false)
    public Integer transferAmount;

    @Column(name = "transfer_time", nullable = false)
    public Timestamp transferTime;

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

    public Long getTransferId() { return transferId; }
    public Boolean getCompensationLegAApplied() { return compensationLegAApplied; }
    public String getCompensationLegACard() { return compensationLegACard; }
    public Integer getCompensationLegADelta() { return compensationLegADelta; }
    public Boolean getCompensationLegBApplied() { return compensationLegBApplied; }
    public String getCompensationLegBCard() { return compensationLegBCard; }
    public Integer getCompensationLegBDelta() { return compensationLegBDelta; }
}
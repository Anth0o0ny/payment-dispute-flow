package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.domain.RiskScreeningCase
import com.payflow.disputes.transaction.domain.RiskScreeningDecision
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "risk_screening_cases")
class RiskScreeningCaseEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "transaction_id", nullable = false)
    var transactionId: UUID = UUID.randomUUID(),

    @Column(name = "risk_score", nullable = false)
    var riskScore: Int = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "risk_screening_case_reasons",
        joinColumns = [JoinColumn(name = "case_id")]
    )
    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    var riskReasons: MutableList<RiskReason> = mutableListOf(),

    @Column(name = "decision", nullable = false)
    @Enumerated(EnumType.STRING)
    var decision: RiskScreeningDecision = RiskScreeningDecision.REQUIRES_REVIEW,

    @Column(name = "screened_at", nullable = false)
    var screenedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): RiskScreeningCase =
        RiskScreeningCase(
            id = id,
            transactionId = transactionId,
            riskScore = riskScore,
            riskReasons = riskReasons.toList(),
            decision = decision,
            screenedAt = screenedAt
        )

    companion object {
        fun from(case: RiskScreeningCase): RiskScreeningCaseEntity =
            RiskScreeningCaseEntity(
                id = case.id,
                transactionId = case.transactionId,
                riskScore = case.riskScore,
                riskReasons = case.riskReasons.toMutableList(),
                decision = case.decision,
                screenedAt = case.screenedAt
            )
    }
}

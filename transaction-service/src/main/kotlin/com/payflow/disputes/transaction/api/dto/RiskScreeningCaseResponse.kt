package com.payflow.disputes.transaction.api.dto

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.domain.RiskScreeningCase
import com.payflow.disputes.transaction.domain.RiskScreeningDecision
import java.time.Instant
import java.util.UUID

data class RiskScreeningCaseResponse(
    val id: UUID,
    val transactionId: UUID,
    val riskScore: Int,
    val riskReasons: List<RiskReason>,
    val decision: RiskScreeningDecision,
    val screenedAt: Instant
) {
    companion object {
        fun from(case: RiskScreeningCase): RiskScreeningCaseResponse =
            RiskScreeningCaseResponse(
                id = case.id,
                transactionId = case.transactionId,
                riskScore = case.riskScore,
                riskReasons = case.riskReasons,
                decision = case.decision,
                screenedAt = case.screenedAt
            )
    }
}

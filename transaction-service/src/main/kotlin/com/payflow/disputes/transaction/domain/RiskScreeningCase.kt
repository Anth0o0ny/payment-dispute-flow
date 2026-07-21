package com.payflow.disputes.transaction.domain

import java.time.Instant
import java.util.UUID

data class RiskScreeningCase(
    val id: UUID,
    val transactionId: UUID,
    val riskScore: Int,
    val riskReasons: List<RiskReason>,
    val decision: RiskScreeningDecision,
    val screenedAt: Instant
)

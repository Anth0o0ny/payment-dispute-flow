package com.payflow.disputes.transaction.service.event

import com.payflow.disputes.transaction.domain.RiskReason
import java.time.Instant
import java.util.UUID

data class SuspiciousTransactionDetectedEvent(
    val eventId: UUID,
    val suspiciousTransactionId: UUID,
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String,
    val customerAge: Int?,
    val channel: String,
    val riskScore: Int,
    val riskReasons: List<RiskReason>,
    val detectedAt: Instant
)

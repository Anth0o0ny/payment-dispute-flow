package com.payflow.disputes.workflow.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
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
    val riskReasons: List<String>,
    val detectedAt: Instant
)

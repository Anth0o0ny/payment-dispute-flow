package com.payflow.disputes.workflow.domain

import java.time.Instant
import java.util.UUID

data class ReviewCase(
    val id: UUID,
    val transactionId: UUID,
    val sourceEventId: UUID,
    val riskScore: Int,
    val riskReasons: List<String>,
    val status: ReviewCaseStatus,
    val receivedAt: Instant,
    val updatedAt: Instant
)

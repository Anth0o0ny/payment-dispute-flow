package com.payflow.disputes.workflow.service.command

import java.time.Instant
import java.util.UUID

data class CreateReviewCaseCommand(
    val transactionId: UUID,
    val sourceEventId: UUID,
    val riskScore: Int,
    val riskReasons: List<String>,
    val receivedAt: Instant
)

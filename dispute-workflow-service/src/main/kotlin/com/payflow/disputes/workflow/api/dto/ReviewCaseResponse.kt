package com.payflow.disputes.workflow.api.dto

import com.payflow.disputes.workflow.domain.ReviewCase
import com.payflow.disputes.workflow.domain.ReviewCaseStatus
import java.time.Instant
import java.util.UUID

data class ReviewCaseResponse(
    val id: UUID,
    val transactionId: UUID,
    val sourceEventId: UUID,
    val riskScore: Int,
    val riskReasons: List<String>,
    val status: ReviewCaseStatus,
    val receivedAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(case: ReviewCase): ReviewCaseResponse =
            ReviewCaseResponse(
                id = case.id,
                transactionId = case.transactionId,
                sourceEventId = case.sourceEventId,
                riskScore = case.riskScore,
                riskReasons = case.riskReasons,
                status = case.status,
                receivedAt = case.receivedAt,
                updatedAt = case.updatedAt
            )
    }
}

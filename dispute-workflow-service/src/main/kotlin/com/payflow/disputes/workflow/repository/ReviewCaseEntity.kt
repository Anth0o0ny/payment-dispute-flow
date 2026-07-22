package com.payflow.disputes.workflow.repository

import com.payflow.disputes.workflow.domain.ReviewCase
import com.payflow.disputes.workflow.domain.ReviewCaseStatus
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
@Table(name = "review_cases")
class ReviewCaseEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "transaction_id", nullable = false)
    var transactionId: UUID = UUID.randomUUID(),

    @Column(name = "source_event_id", nullable = false)
    var sourceEventId: UUID = UUID.randomUUID(),

    @Column(name = "risk_score", nullable = false)
    var riskScore: Int = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "review_case_reasons",
        joinColumns = [JoinColumn(name = "review_case_id")]
    )
    @Column(name = "reason", nullable = false)
    var riskReasons: MutableList<String> = mutableListOf(),

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ReviewCaseStatus = ReviewCaseStatus.RECEIVED,

    @Column(name = "received_at", nullable = false)
    var receivedAt: Instant = Instant.EPOCH,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
) {
    fun toDomain(): ReviewCase =
        ReviewCase(
            id = id,
            transactionId = transactionId,
            sourceEventId = sourceEventId,
            riskScore = riskScore,
            riskReasons = riskReasons.toList(),
            status = status,
            receivedAt = receivedAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(case: ReviewCase): ReviewCaseEntity =
            ReviewCaseEntity(
                id = case.id,
                transactionId = case.transactionId,
                sourceEventId = case.sourceEventId,
                riskScore = case.riskScore,
                riskReasons = case.riskReasons.toMutableList(),
                status = case.status,
                receivedAt = case.receivedAt,
                updatedAt = case.updatedAt
            )
    }
}

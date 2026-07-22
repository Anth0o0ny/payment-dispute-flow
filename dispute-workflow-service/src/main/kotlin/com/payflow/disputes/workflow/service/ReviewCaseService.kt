package com.payflow.disputes.workflow.service

import com.payflow.disputes.workflow.domain.ReviewCase
import com.payflow.disputes.workflow.domain.ReviewCaseStatus
import com.payflow.disputes.workflow.service.command.CreateReviewCaseCommand
import com.payflow.disputes.workflow.service.port.ReviewCaseRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ReviewCaseService(
    private val reviewCaseRepository: ReviewCaseRepository
) {
    fun create(command: CreateReviewCaseCommand): ReviewCase {
        require(command.riskScore > 0) { "riskScore must be positive" }
        require(command.riskReasons.isNotEmpty()) { "riskReasons must not be empty" }

        val case = ReviewCase(
            id = UUID.randomUUID(),
            transactionId = command.transactionId,
            sourceEventId = command.sourceEventId,
            riskScore = command.riskScore,
            riskReasons = command.riskReasons,
            status = ReviewCaseStatus.RECEIVED,
            receivedAt = command.receivedAt,
            updatedAt = command.receivedAt
        )

        return reviewCaseRepository.save(case)
    }

    fun findById(id: UUID): ReviewCase? =
        reviewCaseRepository.findById(id)

    fun findAll(): List<ReviewCase> =
        reviewCaseRepository.findAll()
}

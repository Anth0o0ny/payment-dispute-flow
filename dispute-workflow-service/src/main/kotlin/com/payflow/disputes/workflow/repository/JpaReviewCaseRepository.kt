package com.payflow.disputes.workflow.repository

import com.payflow.disputes.workflow.domain.ReviewCase
import com.payflow.disputes.workflow.service.port.ReviewCaseRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaReviewCaseRepository(
    private val reviewCaseJpaRepository: ReviewCaseJpaRepository
) : ReviewCaseRepository {
    override fun save(case: ReviewCase): ReviewCase =
        reviewCaseJpaRepository.save(ReviewCaseEntity.from(case)).toDomain()

    override fun findById(id: UUID): ReviewCase? =
        reviewCaseJpaRepository.findById(id).map(ReviewCaseEntity::toDomain).orElse(null)

    override fun findAll(): List<ReviewCase> =
        reviewCaseJpaRepository.findAllByOrderByReceivedAtDesc().map(ReviewCaseEntity::toDomain)
}

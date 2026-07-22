package com.payflow.disputes.workflow.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReviewCaseJpaRepository : JpaRepository<ReviewCaseEntity, UUID> {
    fun findAllByOrderByReceivedAtDesc(): List<ReviewCaseEntity>
}

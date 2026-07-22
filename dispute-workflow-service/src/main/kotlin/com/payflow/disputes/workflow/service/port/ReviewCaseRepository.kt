package com.payflow.disputes.workflow.service.port

import com.payflow.disputes.workflow.domain.ReviewCase
import java.util.UUID

interface ReviewCaseRepository {
    fun save(case: ReviewCase): ReviewCase

    fun findById(id: UUID): ReviewCase?

    fun findAll(): List<ReviewCase>
}

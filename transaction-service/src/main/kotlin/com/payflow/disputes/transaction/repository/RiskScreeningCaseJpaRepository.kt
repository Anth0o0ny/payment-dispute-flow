package com.payflow.disputes.transaction.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RiskScreeningCaseJpaRepository : JpaRepository<RiskScreeningCaseEntity, UUID> {
    fun findAllByOrderByScreenedAtDesc(): List<RiskScreeningCaseEntity>
}

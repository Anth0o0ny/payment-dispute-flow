package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.RiskScreeningCase
import com.payflow.disputes.transaction.service.port.RiskScreeningCaseRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaRiskScreeningCaseRepository(
    private val riskScreeningCaseJpaRepository: RiskScreeningCaseJpaRepository
) : RiskScreeningCaseRepository {
    override fun save(case: RiskScreeningCase): RiskScreeningCase =
        riskScreeningCaseJpaRepository.save(RiskScreeningCaseEntity.from(case)).toDomain()

    override fun findById(id: UUID): RiskScreeningCase? =
        riskScreeningCaseJpaRepository.findById(id).map(RiskScreeningCaseEntity::toDomain).orElse(null)

    override fun findAll(): List<RiskScreeningCase> =
        riskScreeningCaseJpaRepository.findAllByOrderByScreenedAtDesc().map(RiskScreeningCaseEntity::toDomain)
}

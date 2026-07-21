package com.payflow.disputes.transaction.service.port

import com.payflow.disputes.transaction.domain.RiskScreeningCase
import java.util.UUID

interface RiskScreeningCaseRepository {
    fun save(case: RiskScreeningCase): RiskScreeningCase

    fun findById(id: UUID): RiskScreeningCase?

    fun findAll(): List<RiskScreeningCase>
}

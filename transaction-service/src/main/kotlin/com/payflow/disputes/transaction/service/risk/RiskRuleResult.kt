package com.payflow.disputes.transaction.service.risk

import com.payflow.disputes.transaction.domain.RiskReason

data class RiskRuleResult(
    val score: Int,
    val reason: RiskReason
)

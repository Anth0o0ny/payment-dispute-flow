package com.payflow.disputes.transaction.risk

import org.springframework.stereotype.Service

@Service
class TransactionRiskService(
    private val riskRules: List<TransactionRiskRule>
) {
    fun assess(input: TransactionRiskInput): TransactionRiskAssessment {
        val results = riskRules.mapNotNull { rule -> rule.check(input) }

        return TransactionRiskAssessment(
            score = results.sumOf { it.score },
            reasons = results.map { it.reason }
        )
    }
}

package com.payflow.disputes.transaction.risk

import com.payflow.disputes.transaction.domain.RiskReason
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(10)
class HighAmountRiskRule : TransactionRiskRule {
    override fun check(input: TransactionRiskInput): RiskRuleResult? =
        if (input.amount >= 100_000) {
            RiskRuleResult(score = 60, reason = RiskReason.HIGH_AMOUNT)
        } else {
            null
        }
}

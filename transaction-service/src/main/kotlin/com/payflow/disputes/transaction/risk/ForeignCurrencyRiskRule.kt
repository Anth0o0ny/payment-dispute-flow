package com.payflow.disputes.transaction.risk

import com.payflow.disputes.transaction.domain.RiskReason
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(40)
class ForeignCurrencyRiskRule : TransactionRiskRule {
    override fun check(input: TransactionRiskInput): RiskRuleResult? =
        if (input.currency != "RUB") {
            RiskRuleResult(score = 25, reason = RiskReason.FOREIGN_CURRENCY)
        } else {
            null
        }
}

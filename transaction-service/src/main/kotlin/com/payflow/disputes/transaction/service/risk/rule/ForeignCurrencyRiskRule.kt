package com.payflow.disputes.transaction.service.risk.rule

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.service.risk.RiskRuleResult
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput
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

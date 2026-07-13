package com.payflow.disputes.transaction.service.risk.rule

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.service.risk.RiskRuleResult
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(20)
class ElderlyCustomerTransferRiskRule : TransactionRiskRule {
    override fun check(input: TransactionRiskInput): RiskRuleResult? =
        if (input.customerAge != null && input.customerAge >= 70 && input.amount >= 30_000) {
            RiskRuleResult(score = 35, reason = RiskReason.ELDERLY_CUSTOMER_TRANSFER)
        } else {
            null
        }
}

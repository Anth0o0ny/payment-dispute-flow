package com.payflow.disputes.transaction.service.risk.rule

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.service.risk.RiskRuleResult
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(50)
class UnknownChannelRiskRule : TransactionRiskRule {
    private val knownChannels = setOf("MOBILE", "WEB", "ATM", "POS")

    override fun check(input: TransactionRiskInput): RiskRuleResult? =
        if (input.channel !in knownChannels) {
            RiskRuleResult(score = 20, reason = RiskReason.UNKNOWN_CHANNEL)
        } else {
            null
        }
}

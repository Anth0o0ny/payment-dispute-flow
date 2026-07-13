package com.payflow.disputes.transaction.service.risk.rule

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.service.risk.RiskRuleResult
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(30)
class RiskyMerchantRiskRule : TransactionRiskRule {
    private val riskyMerchantKeywords = setOf("crypto", "casino", "betting", "unknown")

    override fun check(input: TransactionRiskInput): RiskRuleResult? {
        val normalizedMerchant = input.merchant.lowercase()

        return if (riskyMerchantKeywords.any(normalizedMerchant::contains)) {
            RiskRuleResult(score = 45, reason = RiskReason.RISKY_MERCHANT)
        } else {
            null
        }
    }
}

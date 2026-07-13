package com.payflow.disputes.transaction.service.risk.rule

import com.payflow.disputes.transaction.service.risk.RiskRuleResult
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput

interface TransactionRiskRule {
    fun check(input: TransactionRiskInput): RiskRuleResult?
}

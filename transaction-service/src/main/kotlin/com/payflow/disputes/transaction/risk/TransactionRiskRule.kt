package com.payflow.disputes.transaction.risk

interface TransactionRiskRule {
    fun check(input: TransactionRiskInput): RiskRuleResult?
}

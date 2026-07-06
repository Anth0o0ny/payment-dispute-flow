package com.payflow.disputes.transaction.risk

import com.payflow.disputes.transaction.domain.RiskReason

data class TransactionRiskAssessment(
    val score: Int,
    val reasons: List<RiskReason>
) {
    val suspicious: Boolean
        get() = score >= SUSPICIOUS_THRESHOLD

    companion object {
        private const val SUSPICIOUS_THRESHOLD = 70
    }
}

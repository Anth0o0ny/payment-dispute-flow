package com.payflow.disputes.transaction.api

import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import com.payflow.disputes.transaction.domain.RiskReason
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String,
    val customerAge: Int?,
    val channel: String,
    val riskScore: Int,
    val riskReasons: List<RiskReason>,
    val status: TransactionStatus,
    val createdAt: Instant
) {
    companion object {
        fun from(transaction: Transaction): TransactionResponse =
            TransactionResponse(
                id = transaction.id,
                accountId = transaction.accountId,
                merchant = transaction.merchant,
                amount = transaction.amount,
                currency = transaction.currency,
                customerAge = transaction.customerAge,
                channel = transaction.channel,
                riskScore = transaction.riskScore,
                riskReasons = transaction.riskReasons,
                status = transaction.status,
                createdAt = transaction.createdAt
            )
    }
}

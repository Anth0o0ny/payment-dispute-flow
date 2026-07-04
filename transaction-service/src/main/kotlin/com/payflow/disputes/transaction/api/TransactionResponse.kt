package com.payflow.disputes.transaction.api

import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String,
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
                status = transaction.status,
                createdAt = transaction.createdAt
            )
    }
}

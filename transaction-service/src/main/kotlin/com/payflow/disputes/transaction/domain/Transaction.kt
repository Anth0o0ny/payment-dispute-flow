package com.payflow.disputes.transaction.domain

import java.time.Instant
import java.util.UUID

data class Transaction(
    val id: UUID,
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String,
    val status: TransactionStatus,
    val createdAt: Instant
)

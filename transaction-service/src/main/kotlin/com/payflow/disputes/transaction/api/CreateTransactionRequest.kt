package com.payflow.disputes.transaction.api

data class CreateTransactionRequest(
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String
)

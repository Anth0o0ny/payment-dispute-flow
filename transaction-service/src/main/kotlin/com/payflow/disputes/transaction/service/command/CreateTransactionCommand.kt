package com.payflow.disputes.transaction.service.command

data class CreateTransactionCommand(
    val accountId: String,
    val merchant: String,
    val amount: Long,
    val currency: String,
    val customerAge: Int? = null,
    val channel: String? = null
)

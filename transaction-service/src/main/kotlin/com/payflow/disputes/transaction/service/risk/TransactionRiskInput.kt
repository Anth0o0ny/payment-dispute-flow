package com.payflow.disputes.transaction.service.risk

data class TransactionRiskInput(
    val merchant: String,
    val amount: Long,
    val currency: String,
    val customerAge: Int?,
    val channel: String
)

package com.payflow.disputes.transaction.domain

enum class TransactionStatus {
    NEW,
    SUSPICIOUS,
    DISPUTE_OPENED,
    DISPUTE_APPROVED,
    DISPUTE_REJECTED
}

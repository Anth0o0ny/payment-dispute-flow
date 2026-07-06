package com.payflow.disputes.transaction.domain

enum class RiskReason {
    HIGH_AMOUNT,
    ELDERLY_CUSTOMER_TRANSFER,
    RISKY_MERCHANT,
    FOREIGN_CURRENCY,
    UNKNOWN_CHANNEL
}

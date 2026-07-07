package com.payflow.disputes.transaction.api.error

import java.time.Instant

data class ApiError(
    val message: String,
    val timestamp: Instant = Instant.now()
)

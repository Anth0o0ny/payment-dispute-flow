package com.payflow.disputes.transaction.api

import java.time.Instant

data class ApiError(
    val message: String,
    val timestamp: Instant = Instant.now()
)

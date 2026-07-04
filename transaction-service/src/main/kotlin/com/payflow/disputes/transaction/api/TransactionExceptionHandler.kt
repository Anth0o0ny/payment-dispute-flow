package com.payflow.disputes.transaction.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class TransactionExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(exception: IllegalArgumentException): ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiError(exception.message ?: "Invalid request"))
}

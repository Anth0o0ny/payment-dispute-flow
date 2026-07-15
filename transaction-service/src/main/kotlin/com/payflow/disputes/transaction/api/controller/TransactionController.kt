package com.payflow.disputes.transaction.api.controller

import com.payflow.disputes.transaction.api.dto.CreateTransactionRequest
import com.payflow.disputes.transaction.api.dto.TransactionResponse
import com.payflow.disputes.transaction.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping("/api/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateTransactionRequest): TransactionResponse =
        TransactionResponse.from(transactionService.create(request))

    @GetMapping("/api/suspicious-transactions/{id}")
    fun findSuspiciousById(@PathVariable id: UUID): TransactionResponse =
        transactionService.findSuspiciousById(id)
            ?.let(TransactionResponse::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Suspicious transaction $id was not found")

    @GetMapping("/api/suspicious-transactions")
    fun findAllSuspicious(): List<TransactionResponse> =
        transactionService.findAllSuspicious().map(TransactionResponse::from)
}

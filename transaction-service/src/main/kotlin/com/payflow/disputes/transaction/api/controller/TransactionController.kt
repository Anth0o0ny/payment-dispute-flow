package com.payflow.disputes.transaction.api.controller

import com.payflow.disputes.transaction.api.dto.CreateTransactionRequest
import com.payflow.disputes.transaction.api.dto.TransactionResponse
import com.payflow.disputes.transaction.service.TransactionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateTransactionRequest): TransactionResponse =
        TransactionResponse.from(transactionService.create(request))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): TransactionResponse =
        transactionService.findById(id)
            ?.let(TransactionResponse::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction $id was not found")

    @GetMapping
    fun findAll(): List<TransactionResponse> =
        transactionService.findAll().map(TransactionResponse::from)
}

package com.payflow.disputes.transaction.api.controller

import com.payflow.disputes.transaction.api.dto.CreateTransactionRequest
import com.payflow.disputes.transaction.api.dto.RiskScreeningCaseResponse
import com.payflow.disputes.transaction.api.dto.TransactionResponse
import com.payflow.disputes.transaction.service.TransactionService
import com.payflow.disputes.transaction.service.command.CreateTransactionCommand
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class TransactionController(
    private val transactionService: TransactionService
) {
    @PostMapping("/api/transactions")
    fun create(@RequestBody request: CreateTransactionRequest): TransactionResponse =
        TransactionResponse.from(transactionService.create(request.toCommand()))

    @GetMapping("/api/risk-screening-cases/{id}")
    fun findRiskScreeningCaseById(@PathVariable id: UUID): RiskScreeningCaseResponse =
        transactionService.findRiskScreeningCaseById(id)
            ?.let(RiskScreeningCaseResponse::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Risk screening case $id was not found")

    @GetMapping("/api/risk-screening-cases")
    fun findAllRiskScreeningCases(): List<RiskScreeningCaseResponse> =
        transactionService.findAllRiskScreeningCases().map(RiskScreeningCaseResponse::from)

    private fun CreateTransactionRequest.toCommand(): CreateTransactionCommand =
        CreateTransactionCommand(
            accountId = accountId,
            merchant = merchant,
            amount = amount,
            currency = currency,
            customerAge = customerAge,
            channel = channel
        )
}

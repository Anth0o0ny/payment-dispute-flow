package com.payflow.disputes.transaction.service

import com.payflow.disputes.transaction.api.dto.CreateTransactionRequest
import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import com.payflow.disputes.transaction.repository.SuspiciousTransactionRepository
import com.payflow.disputes.transaction.service.risk.TransactionRiskInput
import com.payflow.disputes.transaction.service.risk.TransactionRiskService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TransactionService(
    private val suspiciousTransactionRepository: SuspiciousTransactionRepository,
    private val transactionRiskService: TransactionRiskService
) {
    fun create(request: CreateTransactionRequest): Transaction {
        require(request.accountId.isNotBlank()) { "accountId must not be blank" }
        require(request.merchant.isNotBlank()) { "merchant must not be blank" }
        require(request.amount > 0) { "amount must be positive" }
        require(request.currency.isNotBlank()) { "currency must not be blank" }
        require(request.customerAge == null || request.customerAge in 0..120) {
            "customerAge must be between 0 and 120"
        }

        val currency = request.currency.trim().uppercase()
        val channel = request.channel?.trim()?.uppercase().takeUnless { it.isNullOrBlank() } ?: "UNKNOWN"
        val riskAssessment = transactionRiskService.assess(
            TransactionRiskInput(
                merchant = request.merchant.trim(),
                amount = request.amount,
                currency = currency,
                customerAge = request.customerAge,
                channel = channel
            )
        )

        val transaction = Transaction(
            id = UUID.randomUUID(),
            accountId = request.accountId.trim(),
            merchant = request.merchant.trim(),
            amount = request.amount,
            currency = currency,
            customerAge = request.customerAge,
            channel = channel,
            riskScore = riskAssessment.score,
            riskReasons = riskAssessment.reasons,
            status = if (riskAssessment.suspicious) TransactionStatus.SUSPICIOUS else TransactionStatus.NEW,
            createdAt = Instant.now()
        )

        return if (transaction.status == TransactionStatus.SUSPICIOUS) {
            suspiciousTransactionRepository.saveSuspicious(transaction)
        } else {
            transaction
        }
    }

    fun findSuspiciousById(id: UUID): Transaction? =
        suspiciousTransactionRepository.findById(id)

    fun findAllSuspicious(): List<Transaction> =
        suspiciousTransactionRepository.findAll()
}

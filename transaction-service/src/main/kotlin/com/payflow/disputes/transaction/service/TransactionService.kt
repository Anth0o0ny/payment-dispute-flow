package com.payflow.disputes.transaction.service

import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import com.payflow.disputes.transaction.service.command.CreateTransactionCommand
import com.payflow.disputes.transaction.service.port.SuspiciousTransactionRepository
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
    fun create(command: CreateTransactionCommand): Transaction {
        require(command.accountId.isNotBlank()) { "accountId must not be blank" }
        require(command.merchant.isNotBlank()) { "merchant must not be blank" }
        require(command.amount > 0) { "amount must be positive" }
        require(command.currency.isNotBlank()) { "currency must not be blank" }
        require(command.customerAge == null || command.customerAge in 0..120) {
            "customerAge must be between 0 and 120"
        }

        val currency = command.currency.trim().uppercase()
        val channel = command.channel?.trim()?.uppercase().takeUnless { it.isNullOrBlank() } ?: "UNKNOWN"
        val riskAssessment = transactionRiskService.assess(
            TransactionRiskInput(
                merchant = command.merchant.trim(),
                amount = command.amount,
                currency = currency,
                customerAge = command.customerAge,
                channel = channel
            )
        )

        val transaction = Transaction(
            id = UUID.randomUUID(),
            accountId = command.accountId.trim(),
            merchant = command.merchant.trim(),
            amount = command.amount,
            currency = currency,
            customerAge = command.customerAge,
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

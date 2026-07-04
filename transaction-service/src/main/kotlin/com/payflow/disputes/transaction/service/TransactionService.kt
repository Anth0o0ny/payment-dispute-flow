package com.payflow.disputes.transaction.service

import com.payflow.disputes.transaction.api.CreateTransactionRequest
import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import com.payflow.disputes.transaction.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository
) {
    fun create(request: CreateTransactionRequest): Transaction {
        require(request.accountId.isNotBlank()) { "accountId must not be blank" }
        require(request.merchant.isNotBlank()) { "merchant must not be blank" }
        require(request.amount > 0) { "amount must be positive" }
        require(request.currency.isNotBlank()) { "currency must not be blank" }

        val transaction = Transaction(
            id = UUID.randomUUID(),
            accountId = request.accountId.trim(),
            merchant = request.merchant.trim(),
            amount = request.amount,
            currency = request.currency.trim().uppercase(),
            status = TransactionStatus.NEW,
            createdAt = Instant.now()
        )

        return transactionRepository.save(transaction)
    }

    fun findById(id: UUID): Transaction? =
        transactionRepository.findById(id)

    fun findAll(): List<Transaction> =
        transactionRepository.findAll()
}

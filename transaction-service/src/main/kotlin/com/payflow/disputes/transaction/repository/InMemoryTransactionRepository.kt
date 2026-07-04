package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.Transaction
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryTransactionRepository : TransactionRepository {
    private val transactions = ConcurrentHashMap<UUID, Transaction>()

    override fun save(transaction: Transaction): Transaction {
        transactions[transaction.id] = transaction
        return transaction
    }

    override fun findById(id: UUID): Transaction? =
        transactions[id]

    override fun findAll(): List<Transaction> =
        transactions.values.sortedByDescending { it.createdAt }
}

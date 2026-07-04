package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.Transaction
import java.util.UUID

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction

    fun findById(id: UUID): Transaction?

    fun findAll(): List<Transaction>
}

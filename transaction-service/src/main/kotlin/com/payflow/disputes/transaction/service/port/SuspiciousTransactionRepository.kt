package com.payflow.disputes.transaction.service.port

import com.payflow.disputes.transaction.domain.Transaction
import java.util.UUID

interface SuspiciousTransactionRepository {
    fun saveSuspicious(transaction: Transaction): Transaction

    fun findById(id: UUID): Transaction?

    fun findAll(): List<Transaction>
}

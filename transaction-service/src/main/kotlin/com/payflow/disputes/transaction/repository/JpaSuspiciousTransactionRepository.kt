package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.service.port.SuspiciousTransactionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaSuspiciousTransactionRepository(
    private val suspiciousTransactionJpaRepository: SuspiciousTransactionJpaRepository
) : SuspiciousTransactionRepository {
    override fun saveSuspicious(transaction: Transaction): Transaction =
        suspiciousTransactionJpaRepository.save(SuspiciousTransactionEntity.from(transaction)).toDomain()

    override fun findById(id: UUID): Transaction? =
        suspiciousTransactionJpaRepository.findById(id).map(SuspiciousTransactionEntity::toDomain).orElse(null)

    override fun findAll(): List<Transaction> =
        suspiciousTransactionJpaRepository.findAllByOrderByCreatedAtDesc().map(SuspiciousTransactionEntity::toDomain)
}

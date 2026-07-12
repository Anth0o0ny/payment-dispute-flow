package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.Transaction
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaTransactionRepository(
    private val transactionJpaRepository: TransactionJpaRepository
) : TransactionRepository {
    override fun save(transaction: Transaction): Transaction =
        transactionJpaRepository.save(TransactionEntity.from(transaction)).toDomain()

    override fun findById(id: UUID): Transaction? =
        transactionJpaRepository.findById(id).map(TransactionEntity::toDomain).orElse(null)

    override fun findAll(): List<Transaction> =
        transactionJpaRepository.findAllByOrderByCreatedAtDesc().map(TransactionEntity::toDomain)
}

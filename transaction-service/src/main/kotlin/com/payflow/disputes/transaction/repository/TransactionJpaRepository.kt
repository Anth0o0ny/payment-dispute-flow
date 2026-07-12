package com.payflow.disputes.transaction.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionJpaRepository : JpaRepository<TransactionEntity, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<TransactionEntity>
}

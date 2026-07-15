package com.payflow.disputes.transaction.repository

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SuspiciousTransactionJpaRepository : JpaRepository<SuspiciousTransactionEntity, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<SuspiciousTransactionEntity>
}

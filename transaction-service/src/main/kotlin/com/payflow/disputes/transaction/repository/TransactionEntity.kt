package com.payflow.disputes.transaction.repository

import com.payflow.disputes.transaction.domain.RiskReason
import com.payflow.disputes.transaction.domain.Transaction
import com.payflow.disputes.transaction.domain.TransactionStatus
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "account_id", nullable = false)
    var accountId: String = "",

    @Column(name = "merchant", nullable = false)
    var merchant: String = "",

    @Column(name = "amount", nullable = false)
    var amount: Long = 0,

    @Column(name = "currency", nullable = false)
    var currency: String = "",

    @Column(name = "customer_age")
    var customerAge: Int? = null,

    @Column(name = "channel", nullable = false)
    var channel: String = "",

    @Column(name = "risk_score", nullable = false)
    var riskScore: Int = 0,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "transaction_risk_reasons",
        joinColumns = [JoinColumn(name = "transaction_id")]
    )
    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    var riskReasons: MutableList<RiskReason> = mutableListOf(),

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: TransactionStatus = TransactionStatus.NEW,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH
) {
    fun toDomain(): Transaction =
        Transaction(
            id = id,
            accountId = accountId,
            merchant = merchant,
            amount = amount,
            currency = currency,
            customerAge = customerAge,
            channel = channel,
            riskScore = riskScore,
            riskReasons = riskReasons.toList(),
            status = status,
            createdAt = createdAt
        )

    companion object {
        fun from(transaction: Transaction): TransactionEntity =
            TransactionEntity(
                id = transaction.id,
                accountId = transaction.accountId,
                merchant = transaction.merchant,
                amount = transaction.amount,
                currency = transaction.currency,
                customerAge = transaction.customerAge,
                channel = transaction.channel,
                riskScore = transaction.riskScore,
                riskReasons = transaction.riskReasons.toMutableList(),
                status = transaction.status,
                createdAt = transaction.createdAt
            )
    }
}

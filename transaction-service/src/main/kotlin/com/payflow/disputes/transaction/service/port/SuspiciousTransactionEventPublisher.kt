package com.payflow.disputes.transaction.service.port

import com.payflow.disputes.transaction.service.event.SuspiciousTransactionDetectedEvent

interface SuspiciousTransactionEventPublisher {
    fun publish(event: SuspiciousTransactionDetectedEvent)
}

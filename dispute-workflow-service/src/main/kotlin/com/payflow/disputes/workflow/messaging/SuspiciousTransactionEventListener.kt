package com.payflow.disputes.workflow.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SuspiciousTransactionEventListener {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.topics.suspicious-transactions}"],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consume(event: SuspiciousTransactionDetectedEvent) {
        log.info(
            "Received suspicious transaction event: eventId={}, transactionId={}, riskScore={}, reasons={}",
            event.eventId,
            event.suspiciousTransactionId,
            event.riskScore,
            event.riskReasons
        )
    }
}

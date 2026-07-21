package com.payflow.disputes.transaction.messaging

import com.payflow.disputes.transaction.service.event.SuspiciousTransactionDetectedEvent
import com.payflow.disputes.transaction.service.port.SuspiciousTransactionEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSuspiciousTransactionEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, SuspiciousTransactionDetectedEvent>,
    @Value("\${app.kafka.topics.suspicious-transactions}") private val topicName: String
) : SuspiciousTransactionEventPublisher {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: SuspiciousTransactionDetectedEvent) {
        kafkaTemplate
            .send(topicName, event.suspiciousTransactionId.toString(), event)
            .whenComplete { _, exception ->
                if (exception == null) {
                    log.info(
                        "Published suspicious transaction event: eventId={}, suspiciousTransactionId={}",
                        event.eventId,
                        event.suspiciousTransactionId
                    )
                } else {
                    log.error(
                        "Failed to publish suspicious transaction event: eventId={}, suspiciousTransactionId={}",
                        event.eventId,
                        event.suspiciousTransactionId,
                        exception
                    )
                }
            }
    }
}

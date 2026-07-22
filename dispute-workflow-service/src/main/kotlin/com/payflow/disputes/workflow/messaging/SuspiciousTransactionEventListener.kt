package com.payflow.disputes.workflow.messaging

import com.payflow.disputes.workflow.service.ReviewCaseService
import com.payflow.disputes.workflow.service.command.CreateReviewCaseCommand
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class SuspiciousTransactionEventListener(
    private val reviewCaseService: ReviewCaseService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.topics.suspicious-transactions}"],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consume(event: SuspiciousTransactionDetectedEvent) {
        val reviewCase = reviewCaseService.create(event.toCreateReviewCaseCommand())

        log.info(
            "Created review case from suspicious transaction event: reviewCaseId={}, eventId={}, transactionId={}, riskScore={}, reasons={}",
            reviewCase.id,
            event.eventId,
            event.suspiciousTransactionId,
            event.riskScore,
            event.riskReasons
        )
    }

    private fun SuspiciousTransactionDetectedEvent.toCreateReviewCaseCommand(): CreateReviewCaseCommand =
        CreateReviewCaseCommand(
            transactionId = suspiciousTransactionId,
            sourceEventId = eventId,
            riskScore = riskScore,
            riskReasons = riskReasons,
            receivedAt = detectedAt
        )
}

package com.payflow.disputes.workflow.api.controller

import com.payflow.disputes.workflow.api.dto.ReviewCaseResponse
import com.payflow.disputes.workflow.service.ReviewCaseService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/review-cases")
class ReviewCaseController(
    private val reviewCaseService: ReviewCaseService
) {
    @GetMapping
    fun findAll(): List<ReviewCaseResponse> =
        reviewCaseService.findAll().map(ReviewCaseResponse::from)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ReviewCaseResponse =
        reviewCaseService.findById(id)
            ?.let(ReviewCaseResponse::from)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Review case $id was not found")
}

package com.araw.araw.presentation;

import com.araw.araw.application.dto.feedback.CreateFeedbackRequest;
import com.araw.araw.application.dto.feedback.FeedbackResponse;
import com.araw.araw.application.dto.feedback.FeedbackSummaryResponse;
import com.araw.araw.application.dto.feedback.UpdateFeedbackRequest;
import com.araw.araw.application.service.FeedbackApplicationService;
import com.araw.araw.domain.feedback.service.FeedbackDomainService;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/araw/feedback")
@Validated
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackApplicationService feedbackService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        return feedbackService.createFeedback(request);
    }

    @PutMapping("/{feedbackId}")
    public FeedbackResponse updateFeedback(@PathVariable UUID feedbackId,
                                           @Valid @RequestBody UpdateFeedbackRequest request) {
        return feedbackService.updateFeedback(feedbackId, request);
    }

    @PostMapping("/{feedbackId}/publish")
    public FeedbackResponse publishFeedback(@PathVariable UUID feedbackId) {
        return feedbackService.publishFeedback(feedbackId);
    }

    @PostMapping("/{feedbackId}/feature")
    public FeedbackResponse featureFeedback(@PathVariable UUID feedbackId,
                                            @RequestParam("value") boolean featured) {
        return feedbackService.featureFeedback(feedbackId, featured);
    }

    @GetMapping("/{feedbackId}")
    public FeedbackResponse getFeedback(@PathVariable UUID feedbackId) {
        return feedbackService.getFeedback(feedbackId);
    }

    @GetMapping
    public PagedResponse<FeedbackSummaryResponse> listFeedback(
            @RequestParam(value = "eventId", required = false) UUID eventId,
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Page<FeedbackSummaryResponse> page = feedbackService.listFeedback(eventId, pageable, search);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/events/{eventId}/insights")
    public FeedbackDomainService.EventFeedbackInsights getInsights(@PathVariable UUID eventId) {
        return feedbackService.getEventInsights(eventId);
    }

    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeedback(@PathVariable UUID feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
    }
}

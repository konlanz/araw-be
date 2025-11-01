package com.araw.araw.presentation;

import com.araw.araw.application.dto.application.ApplicationDecisionRequest;
import com.araw.araw.application.dto.application.ApplicationFilterRequest;
import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.application.ApplicationWaitlistRequest;
import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.application.ReviewApplicationRequest;
import com.araw.araw.application.dto.application.UpdateApplicationRequest;
import com.araw.araw.application.service.ApplicationApplicationService;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/araw/applications")
@Validated
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationApplicationService applicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        return applicationService.createApplication(request);
    }

    @PutMapping("/{applicationId}")
    public ApplicationResponse updateApplication(@PathVariable UUID applicationId,
                                                 @Valid @RequestBody UpdateApplicationRequest request) {
        return applicationService.updateApplication(applicationId, request);
    }

    @PostMapping("/{applicationId}/submit")
    public ApplicationResponse submitApplication(@PathVariable UUID applicationId) {
        return applicationService.submitApplication(applicationId);
    }

    @PostMapping("/{applicationId}/review")
    public ApplicationResponse reviewApplication(@PathVariable UUID applicationId,
                                                 @Valid @RequestBody ReviewApplicationRequest request) {
        return applicationService.reviewApplication(applicationId, request);
    }

    @PostMapping("/{applicationId}/accept")
    public ApplicationResponse acceptApplication(@PathVariable UUID applicationId) {
        return applicationService.acceptApplication(applicationId);
    }

    @PostMapping("/{applicationId}/reject")
    public ApplicationResponse rejectApplication(@PathVariable UUID applicationId,
                                                 @Valid @RequestBody ApplicationDecisionRequest request) {
        return applicationService.rejectApplication(applicationId, request.getReason());
    }

    @PostMapping("/{applicationId}/waitlist")
    public ApplicationResponse waitlistApplication(@PathVariable UUID applicationId,
                                                   @Valid @RequestBody(required = false) ApplicationWaitlistRequest request) {
        Integer position = request != null ? request.getPosition() : null;
        return applicationService.waitlistApplication(applicationId, position);
    }

    @PostMapping("/{applicationId}/confirm")
    public ApplicationResponse confirmApplication(@PathVariable UUID applicationId) {
        return applicationService.confirmApplication(applicationId);
    }

    @PostMapping("/{applicationId}/cancel")
    public ApplicationResponse cancelApplication(@PathVariable UUID applicationId,
                                                 @Valid @RequestBody ApplicationDecisionRequest request) {
        return applicationService.cancelApplication(applicationId, request.getReason());
    }

    @GetMapping("/{applicationId}")
    public ApplicationResponse getApplication(@PathVariable UUID applicationId) {
        return applicationService.getApplication(applicationId);
    }

    @GetMapping
    public PagedResponse<ApplicationResponse> listApplications(
            @RequestParam(value = "eventId", required = false) UUID eventId,
            @RequestParam(value = "status", required = false) ApplicationStatus status,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "guardianConsent", required = false) Boolean guardianConsent,
            @RequestParam(value = "minScore", required = false) Integer minScore,
            @RequestParam(value = "maxScore", required = false) Integer maxScore,
            @RequestParam(value = "submittedAfter", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submittedAfter,
            @RequestParam(value = "submittedBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submittedBefore,
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {

        ApplicationFilterRequest filter = ApplicationFilterRequest.builder()
                .eventId(eventId)
                .status(status)
                .email(email)
                .hasGuardianConsent(guardianConsent)
                .minScore(minScore)
                .maxScore(maxScore)
                .submittedAfter(submittedAfter)
                .submittedBefore(submittedBefore)
                .searchTerm(search)
                .build();

        Page<ApplicationResponse> page = applicationService.searchApplications(filter, pageable);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/{applicationId}/documents/{documentId}/download")
    public Map<String, String> getApplicationDocumentDownloadUrl(@PathVariable UUID applicationId,
                                                                 @PathVariable UUID documentId) {
        String url = applicationService.getDocumentDownloadUrl(applicationId, documentId);
        return Map.of("url", url);
    }
}

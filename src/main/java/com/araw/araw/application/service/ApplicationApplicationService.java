package com.araw.araw.application.service;

import com.araw.araw.application.dto.application.ApplicationFilterRequest;
import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.application.ReviewApplicationRequest;
import com.araw.araw.application.dto.application.UpdateApplicationRequest;
import com.araw.araw.application.mapper.ApplicationMapper;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.notification.template.TemplatedEmailRequest;
import com.araw.notification.template.TemplatedEmailService;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationApplicationService {

    private static final DateTimeFormatter HUMAN_READABLE = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationMapper applicationMapper;
    private final TemplatedEmailService templatedEmailService;
    private final ApplicationDocumentService applicationDocumentService;

    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        if (request.getEventId() == null) {
            throw new DomainValidationException("Event ID is required");
        }
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + request.getEventId()));

        Participant participant = null;
        if (request.getParticipantId() != null) {
            participant = participantRepository.findById(request.getParticipantId())
                    .orElseThrow(() -> new DomainNotFoundException("Participant not found: " + request.getParticipantId()));
        }

        if (applicationRepository.existsByEventIdAndEmail(request.getEventId(), request.getEmail())) {
            throw new DomainValidationException("Application already submitted for this event with the provided email");
        }

        Application application = applicationMapper.toEntity(request);
        application.setEvent(event);
        application.setParticipant(participant);
        application.setEmail(request.getEmail());

        Application saved = applicationRepository.save(application);
        eventRepository.incrementApplicationCount(event.getId());

        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse updateApplication(UUID applicationId, UpdateApplicationRequest request) {
        Application application = getApplicationEntity(applicationId);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(application.getEmail())) {
            if (applicationRepository.existsByEventIdAndEmail(application.getEvent().getId(), request.getEmail())) {
                throw new DomainValidationException("Another application already uses this email for the event");
            }
            application.setEmail(request.getEmail());
        }

        applicationMapper.updateEntity(application, request);
        Application saved = applicationRepository.save(application);
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse submitApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        application.submit();
        Application saved = applicationRepository.save(application);
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse reviewApplication(UUID applicationId, ReviewApplicationRequest request) {
        Application application = getApplicationEntity(applicationId);
        application.review(request.getReviewScore(), request.getReviewNotes(), request.getReviewerName());
        Application saved = applicationRepository.save(application);
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse acceptApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW
                && application.getStatus() != ApplicationStatus.SUBMITTED
                && application.getStatus() != ApplicationStatus.WAITLISTED) {
            throw new DomainValidationException("Application must be under review, submitted, or waitlisted before acceptance");
        }
        application.accept();
        Application saved = applicationRepository.save(application);
        sendStatusEmail(saved, "application-accepted.txt", "You're accepted to {{event.title}}!");
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse rejectApplication(UUID applicationId, String reason) {
        Application application = getApplicationEntity(applicationId);
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("Rejection reason is required");
        }
        application.reject(reason);
        Application saved = applicationRepository.save(application);
        eventRepository.decrementApplicationCount(saved.getEvent().getId());
        sendStatusEmail(saved, "application-rejected.txt", "Update on your {{event.title}} application");
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse waitlistApplication(UUID applicationId, Integer position) {
        Application application = getApplicationEntity(applicationId);
        Integer waitlistPosition = position;
        if (waitlistPosition == null) {
            Integer maxPosition = applicationRepository.findMaxWaitlistPosition(application.getEvent().getId());
            waitlistPosition = maxPosition != null ? maxPosition + 1 : 1;
        }
        application.waitlist(waitlistPosition);
        Application saved = applicationRepository.save(application);
        sendStatusEmail(saved, "application-waitlisted.txt", "{{event.title}} application waitlist update");
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse confirmApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        application.confirm();
        Application saved = applicationRepository.save(application);
        sendStatusEmail(saved, "application-confirmed.txt", "{{event.title}} spot confirmed");
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    public ApplicationResponse cancelApplication(UUID applicationId, String reason) {
        Application application = getApplicationEntity(applicationId);
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("Cancellation reason is required");
        }
        application.cancel(reason);
        Application saved = applicationRepository.save(application);
        eventRepository.decrementApplicationCount(saved.getEvent().getId());
        sendStatusEmail(saved, "application-cancelled.txt", "{{event.title}} application cancelled");
        ApplicationResponse response = applicationMapper.toResponse(saved);
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(UUID applicationId) {
        ApplicationResponse response = applicationMapper.toResponse(getApplicationEntity(applicationId));
        applicationDocumentService.populateDownloadUrls(response);
        return response;
    }

    @Transactional(readOnly = true)
    public String getDocumentDownloadUrl(UUID applicationId, UUID documentId) {
        return applicationDocumentService.generateDownloadUrl(applicationId, documentId);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> searchApplications(ApplicationFilterRequest filter, Pageable pageable) {
        Page<Application> page;
        if (filter != null && filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
            page = applicationRepository.searchApplications(filter.getSearchTerm().trim(), pageable);
        } else if (filter != null) {
            page = applicationRepository.findWithFilters(
                    filter.getEventId(),
                    filter.getStatus(),
                    filter.getEmail(),
                    filter.getHasGuardianConsent(),
                    filter.getMinScore(),
                    filter.getMaxScore(),
                    pageable
            );
        } else {
            page = applicationRepository.findAll(pageable);
        }
        Page<ApplicationResponse> responsePage = page.map(applicationMapper::toResponse);
        applicationDocumentService.populateDownloadUrls(responsePage.getContent());
        return responsePage;
    }

    private Application getApplicationEntity(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DomainNotFoundException("Application not found: " + applicationId));
    }

    private void sendStatusEmail(Application application, String templateName, String subjectTemplate) {
        if (application.getEmail() == null || application.getEmail().isBlank()) {
            return;
        }

        Map<String, Object> variables = buildEmailVariables(application);

        templatedEmailService.send(TemplatedEmailRequest.builder()
                .templateName(templateName)
                .subjectTemplate(subjectTemplate)
                .variables(variables)
                .to(application.getEmail())
                .build());
    }

    private Map<String, Object> buildEmailVariables(Application application) {
        Map<String, Object> variables = new java.util.HashMap<>();

        Map<String, Object> applicant = new java.util.HashMap<>();
        if (application.getApplicantInfo() != null) {
            applicant.put("firstName", application.getApplicantInfo().getFirstName());
            applicant.put("lastName", application.getApplicantInfo().getLastName());
        }
        applicant.put("email", application.getEmail());
        variables.put("applicant", applicant);

        Map<String, Object> event = new java.util.HashMap<>();
        if (application.getEvent() != null) {
            event.put("title", application.getEvent().getTitle());
            event.put("applicationLink", application.getEvent().getApplicationLink());
            event.put("applicationSlug", application.getEvent().getApplicationSlug());
        }
        variables.put("event", event);

        Map<String, Object> applicationData = new java.util.HashMap<>();
        applicationData.put("number", application.getApplicationNumber());
        applicationData.put("status", application.getStatus().name());
        if (application.getSubmittedAt() != null) {
            applicationData.put("submittedAt", application.getSubmittedAt().format(HUMAN_READABLE));
        }
        if (application.getWaitlistPosition() != null) {
            applicationData.put("waitlistPosition", application.getWaitlistPosition());
        }
        if (application.getRejectionReason() != null) {
            applicationData.put("rejectionReason", application.getRejectionReason());
        }
        if (application.getCancellationReason() != null) {
            applicationData.put("cancellationReason", application.getCancellationReason());
        }
        if (application.getConfirmationToken() != null && application.getEvent() != null) {
            String baseUrl = templatedEmailService.getTemplateProperties().getApplicationBaseUrl();
            if (baseUrl != null && !baseUrl.isBlank() && application.getEvent().getApplicationSlug() != null) {
                applicationData.put("confirmationUrl",
                        baseUrl + "/" + application.getEvent().getApplicationSlug() + "/confirm?token=" + application.getConfirmationToken());
            }
            applicationData.put("confirmationToken", application.getConfirmationToken());
        }
        variables.put("application", applicationData);

        return variables;
    }
}

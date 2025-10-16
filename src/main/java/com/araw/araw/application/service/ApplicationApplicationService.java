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
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationApplicationService {

    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationMapper applicationMapper;

    public ApplicationResponse createApplication(CreateApplicationRequest request) {
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

        return applicationMapper.toResponse(saved);
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
        return applicationMapper.toResponse(saved);
    }

    public ApplicationResponse submitApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        application.submit();
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse reviewApplication(UUID applicationId, ReviewApplicationRequest request) {
        Application application = getApplicationEntity(applicationId);
        application.review(request.getReviewScore(), request.getReviewNotes(), request.getReviewerName());
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse acceptApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        if (application.getStatus() != ApplicationStatus.UNDER_REVIEW
                && application.getStatus() != ApplicationStatus.SUBMITTED
                && application.getStatus() != ApplicationStatus.WAITLISTED) {
            throw new DomainValidationException("Application must be under review, submitted, or waitlisted before acceptance");
        }
        application.accept();
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse rejectApplication(UUID applicationId, String reason) {
        Application application = getApplicationEntity(applicationId);
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("Rejection reason is required");
        }
        application.reject(reason);
        Application saved = applicationRepository.save(application);
        eventRepository.decrementApplicationCount(saved.getEvent().getId());
        return applicationMapper.toResponse(saved);
    }

    public ApplicationResponse waitlistApplication(UUID applicationId, Integer position) {
        Application application = getApplicationEntity(applicationId);
        Integer waitlistPosition = position;
        if (waitlistPosition == null) {
            Integer maxPosition = applicationRepository.findMaxWaitlistPosition(application.getEvent().getId());
            waitlistPosition = maxPosition != null ? maxPosition + 1 : 1;
        }
        application.waitlist(waitlistPosition);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse confirmApplication(UUID applicationId) {
        Application application = getApplicationEntity(applicationId);
        application.confirm();
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    public ApplicationResponse cancelApplication(UUID applicationId, String reason) {
        Application application = getApplicationEntity(applicationId);
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("Cancellation reason is required");
        }
        application.cancel(reason);
        Application saved = applicationRepository.save(application);
        eventRepository.decrementApplicationCount(saved.getEvent().getId());
        return applicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(UUID applicationId) {
        return applicationMapper.toResponse(getApplicationEntity(applicationId));
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
        return page.map(applicationMapper::toResponse);
    }

    private Application getApplicationEntity(UUID applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(() -> new DomainNotFoundException("Application not found: " + applicationId));
    }
}

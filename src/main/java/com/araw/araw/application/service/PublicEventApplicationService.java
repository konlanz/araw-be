package com.araw.araw.application.service;

import com.araw.araw.application.dto.application.ApplicationResponse;
import com.araw.araw.application.dto.application.CreateApplicationRequest;
import com.araw.araw.application.dto.participant.ContactInfoDto;
import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.participant.ParticipantResponse;
import com.araw.araw.application.dto.publicapp.PublicEventApplicationRequest;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.entity.EventDate;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.notification.template.TemplatedEmailRequest;
import com.araw.notification.template.TemplatedEmailService;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicEventApplicationService {

    private static final DateTimeFormatter HUMAN_READABLE = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm");

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantApplicationService participantApplicationService;
    private final ApplicationApplicationService applicationApplicationService;
    private final TemplatedEmailService templatedEmailService;

    public ApplicationResponse submitApplication(String applicationSlug,
                                                 PublicEventApplicationRequest request) {
        Event event = eventRepository.findByApplicationSlug(applicationSlug)
                .orElseThrow(() -> new DomainNotFoundException("Event not found for link: " + applicationSlug));
        return submitApplication(event, request);
    }

    public ApplicationResponse submitApplication(UUID eventId,
                                                 PublicEventApplicationRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + eventId));
        return submitApplication(event, request);
    }

    private ApplicationResponse submitApplication(Event event,
                                                  PublicEventApplicationRequest request) {
        validateEventForApplication(event);

        CreateApplicationRequest applicationRequest = request.getApplication();
        applicationRequest.setEventId(event.getId());

        UUID participantId = resolveParticipantId(event, request.getParticipant(), applicationRequest.getEmail());
        applicationRequest.setParticipantId(participantId);

        ApplicationResponse created = applicationApplicationService.createApplication(applicationRequest);
        ApplicationResponse submitted = applicationApplicationService.submitApplication(created.getId());

        dispatchSubmissionEmail(event, submitted);

        return submitted;
    }

    private void validateEventForApplication(Event event) {
        if (!Boolean.TRUE.equals(event.getIsPublished()) || event.getStatus() != EventStatus.UPCOMING) {
            throw new DomainValidationException("This event is not accepting applications right now.");
        }
        if (!isRegistrationCurrentlyOpen(event)) {
            throw new DomainValidationException("Registration for this event is currently closed.");
        }
        if (!event.hasCapacity()) {
            throw new DomainValidationException("The event has reached its capacity.");
        }
    }

    private boolean isRegistrationCurrentlyOpen(Event event) {
        if (event.isRegistrationOpen()) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        if (event.getRegistrationOpensAt() == null && event.getRegistrationClosesAt() == null) {
            return event.getApplicationDeadline() == null || now.isBefore(event.getApplicationDeadline());
        }
        if (event.getRegistrationOpensAt() != null && now.isBefore(event.getRegistrationOpensAt())) {
            return false;
        }
        if (event.getRegistrationClosesAt() != null && now.isAfter(event.getRegistrationClosesAt())) {
            return false;
        }
        if (event.getApplicationDeadline() != null && now.isAfter(event.getApplicationDeadline())) {
            return false;
        }
        return true;
    }

    private UUID resolveParticipantId(Event event, CreateParticipantRequest participantRequest, String fallbackEmail) {
        String email = extractEmail(participantRequest, fallbackEmail);
        if (email == null) {
            throw new DomainValidationException("Applicant email is required to create a participant record");
        }

        Optional<Participant> existing = participantRepository.findByContactInfoEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        if (participantRequest == null) {
            return null;
        }

        ensureContactInfoEmail(participantRequest, email);
        ParticipantResponse response = participantApplicationService.createParticipant(participantRequest);
        dispatchParticipantWelcomeEmail(event, response);
        return response.getId();
    }

    private String extractEmail(CreateParticipantRequest participantRequest, String fallbackEmail) {
        if (participantRequest != null && participantRequest.getContactInfo() != null &&
                participantRequest.getContactInfo().getEmail() != null &&
                !participantRequest.getContactInfo().getEmail().isBlank()) {
            return participantRequest.getContactInfo().getEmail();
        }
        if (fallbackEmail != null && !fallbackEmail.isBlank()) {
            return fallbackEmail.trim();
        }
        return null;
    }

    private void ensureContactInfoEmail(CreateParticipantRequest participantRequest, String email) {
        if (participantRequest.getContactInfo() == null) {
            participantRequest.setContactInfo(ContactInfoDto.builder().email(email).build());
            return;
        }
        if (participantRequest.getContactInfo().getEmail() == null ||
                participantRequest.getContactInfo().getEmail().isBlank()) {
            participantRequest.getContactInfo().setEmail(email);
        }
    }

    private void dispatchSubmissionEmail(Event event, ApplicationResponse application) {
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> applicant = new HashMap<>();
        if (application.getApplicantInfo() != null) {
            applicant.put("firstName", application.getApplicantInfo().getFirstName());
            applicant.put("lastName", application.getApplicantInfo().getLastName());
        }
        applicant.put("email", application.getEmail());
        variables.put("applicant", applicant);

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("title", event.getTitle());
        eventMap.put("applicationLink", event.getApplicationLink());
        eventMap.put("applicationSlug", event.getApplicationSlug());
        if (event.getEventDates() != null && !event.getEventDates().isEmpty()) {
            EventDate first = event.getEventDates().get(0);
            if (first.getSessionDate() != null) {
                eventMap.put("firstSessionDate", first.getSessionDate().format(HUMAN_READABLE));
            }
        }
        variables.put("event", eventMap);

        Map<String, Object> applicationMap = new HashMap<>();
        applicationMap.put("number", application.getApplicationNumber());
        applicationMap.put("status", application.getStatus().name());
        if (application.getSubmittedAt() != null) {
            applicationMap.put("submittedAt", application.getSubmittedAt().format(HUMAN_READABLE));
        }
        variables.put("application", applicationMap);

        templatedEmailService.send(TemplatedEmailRequest.builder()
                .templateName("application-submitted.txt")
                .subjectTemplate("{{event.title}} application received")
                .variables(variables)
                .to(application.getEmail())
                .build());
    }

    private void dispatchParticipantWelcomeEmail(Event event, ParticipantResponse participant) {
        if (participant == null || participant.getContactInfo() == null ||
                participant.getContactInfo().getEmail() == null ||
                participant.getContactInfo().getEmail().isBlank()) {
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> participantMap = new HashMap<>();
        participantMap.put("firstName", participant.getFirstName());
        participantMap.put("lastName", participant.getLastName());
        participantMap.put("preferredName", participant.getPreferredName());
        participantMap.put("displayName", resolveDisplayName(participant));
        participantMap.put("participantCode", participant.getParticipantCode());
        variables.put("participant", participantMap);

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("title", event.getTitle());
        eventMap.put("applicationLink", event.getApplicationLink());
        variables.put("event", eventMap);

        templatedEmailService.send(TemplatedEmailRequest.builder()
                .templateName("participant-welcome.txt")
                .subjectTemplate("Welcome to ARAW, {{participant.firstName}}!")
                .variables(variables)
                .to(participant.getContactInfo().getEmail())
                .build());
    }

    private String resolveDisplayName(ParticipantResponse participant) {
        if (participant.getPreferredName() != null && !participant.getPreferredName().isBlank()) {
            return participant.getPreferredName();
        }
        return participant.getFirstName() != null ? participant.getFirstName() : "there";
    }
}

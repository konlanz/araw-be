package com.araw.araw.domain.event.service;

import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.notification.config.EmailTemplateProperties;
import com.araw.shared.text.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventDomainService {

    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final SlugGenerator slugGenerator;
    private final EmailTemplateProperties emailTemplateProperties;

    public Event publishEvent(Event event) {
        validateEventForPublishing(event);
        ensureApplicationLink(event);
        event.publish();
        return eventRepository.save(event);
    }

    private void validateEventForPublishing(Event event) {
        if (event.getEventDates() == null || event.getEventDates().isEmpty()) {
            throw new IllegalStateException("Event must have at least one date to be published");
        }

        if (event.getApplicationDeadline() != null &&
                event.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Application deadline has already passed");
        }

        if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            throw new IllegalStateException("Event must have a title");
        }

        if (event.getLocation() == null) {
            throw new IllegalStateException("Event must have a location");
        }
    }

    private void ensureApplicationLink(Event event) {
        if (event.getApplicationSlug() == null || event.getApplicationSlug().isBlank()) {
            String baseSlug = slugGenerator.generateSlug(event.getTitle());
            String uniqueSlug = resolveUniqueSlug(baseSlug, event.getId());
            event.setApplicationSlug(uniqueSlug);
        }

        if (event.getApplicationLink() == null || event.getApplicationLink().isBlank()) {
            String baseUrl = emailTemplateProperties.getApplicationBaseUrl();
            if (baseUrl != null && !baseUrl.isBlank()) {
                event.setApplicationLink(baseUrl + "/" + event.getApplicationSlug());
                event.setApplicationLinkGeneratedAt(LocalDateTime.now());
            }
        }
    }

    private String resolveUniqueSlug(String baseSlug, UUID eventId) {
        String candidate = baseSlug;
        int counter = 1;
        while (slugExists(candidate, eventId)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }

    private boolean slugExists(String slug, UUID currentId) {
        return eventRepository.findByApplicationSlug(slug)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .isPresent();
    }


    public Event cancelEvent(UUID eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.cancel(reason);

        applicationRepository.cancelApplicationsForEvent(
                eventId,
                "Event cancelled: " + reason,
                LocalDateTime.now()
        );

        // TODO: Send notifications to affected participants

        return eventRepository.save(event);
    }


    public boolean canParticipantApply(Event event, Participant participant) {
        if (!event.isRegistrationOpen()) {
            return false;
        }

        if (participant.getDateOfBirth() != null) {
            int age = LocalDateTime.now().getYear() - participant.getDateOfBirth().getYear();
            if (event.getMinAge() != null && age < event.getMinAge()) {
                return false;
            }
            if (event.getMaxAge() != null && age > event.getMaxAge()) {
                return false;
            }
        }

        boolean hasApplied = applicationRepository.existsByEventIdAndParticipantId(
                event.getId(), participant.getId()
        );

        return !hasApplied && event.hasCapacity();
    }


    public void processWaitlist(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (!event.hasCapacity()) {
            return;
        }

        List<Application> waitlisted = applicationRepository
                .findWaitlistedByEventOrderByPosition(eventId);

        if (!waitlisted.isEmpty() && event.hasCapacity()) {
            Application nextInLine = waitlisted.get(0);
            nextInLine.accept();
            applicationRepository.save(nextInLine);

            applicationRepository.updateWaitlistPositions(eventId, 1);

            // TODO: Send acceptance notification
        }
    }


    public Event cloneEvent(UUID eventId, String newTitle) {
        Event original = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Event cloned = Event.builder()
                .title(newTitle)
                .description(original.getDescription())
                .shortDescription(original.getShortDescription())
                .eventType(original.getEventType())
                .status(EventStatus.DRAFT)
                .location(copyLocation(original.getLocation()))
                .maxParticipants(original.getMaxParticipants())
                .minAge(original.getMinAge())
                .maxAge(original.getMaxAge())
                .isFree(original.getIsFree())
                .cost(original.getCost())
                .currency(original.getCurrency())
                .prerequisites(copySet(original.getPrerequisites()))
                .learningOutcomes(copySet(original.getLearningOutcomes()))
                .targetGrades(copySet(original.getTargetGrades()))
                .build();

        return eventRepository.save(cloned);
    }


    public double calculateEventCompletion(Event event) {
        if (event.getEventDates().isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        long totalSessions = event.getEventDates().size();
        long completedSessions = event.getEventDates().stream()
                .filter(date -> date.getSessionEndDate() != null &&
                        date.getSessionEndDate().isBefore(now))
                .count();

        return (double) completedSessions / totalSessions * 100;
    }


    public void autoTransitionEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        int started = eventRepository.startEventsWithPastStartDate(now);

        int completed = eventRepository.completeEventsWithPastEndDate(now);

        if (started > 0 || completed > 0) {
            System.out.printf(
                    "Event status transitions: %d started, %d completed%n",
                    started, completed
            );
        }
    }


    public double getCapacityUtilization(Event event) {
        if (event.getMaxParticipants() == null || event.getMaxParticipants() == 0) {
            return 0.0;
        }
        return (double) event.getParticipantCount() / event.getMaxParticipants() * 100;
    }

    private com.araw.araw.domain.event.valueobject.Location copyLocation(com.araw.araw.domain.event.valueobject.Location original) {
        if (original == null) {
            return null;
        }
        return com.araw.araw.domain.event.valueobject.Location.builder()
                .venueName(original.getVenueName())
                .addressLine1(original.getAddressLine1())
                .addressLine2(original.getAddressLine2())
                .city(original.getCity())
                .stateProvince(original.getStateProvince())
                .postalCode(original.getPostalCode())
                .country(original.getCountry())
                .latitude(original.getLatitude())
                .longitude(original.getLongitude())
                .roomNumber(original.getRoomNumber())
                .buildingName(original.getBuildingName())
                .parkingInfo(original.getParkingInfo())
                .accessibilityInfo(original.getAccessibilityInfo())
                .virtualMeetingUrl(original.getVirtualMeetingUrl())
                .virtualMeetingPassword(original.getVirtualMeetingPassword())
                .isVirtual(original.getIsVirtual())
                .isHybrid(original.getIsHybrid())
                .build();
    }

    private <T> HashSet<T> copySet(java.util.Set<T> source) {
        if (source == null) {
            return null;
        }
        return new HashSet<>(source);
    }
}

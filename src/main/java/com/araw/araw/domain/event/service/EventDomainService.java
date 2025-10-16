package com.araw.araw.domain.event.service;

import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.participant.enitity.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventDomainService {

    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;

    public Event publishEvent(Event event) {
        validateEventForPublishing(event);
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
                .location(original.getLocation())
                .maxParticipants(original.getMaxParticipants())
                .minAge(original.getMinAge())
                .maxAge(original.getMaxAge())
                .isFree(original.getIsFree())
                .cost(original.getCost())
                .currency(original.getCurrency())
                .prerequisites(original.getPrerequisites())
                .learningOutcomes(original.getLearningOutcomes())
                .targetGrades(original.getTargetGrades())
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
}


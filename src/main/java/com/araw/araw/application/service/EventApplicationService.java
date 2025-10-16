package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.CreateEventRequest;
import com.araw.araw.application.dto.event.EventDateDto;
import com.araw.araw.application.dto.event.EventResponse;
import com.araw.araw.application.dto.event.EventSummaryResponse;
import com.araw.araw.application.dto.event.UpdateEventRequest;
import com.araw.araw.application.mapper.EventMapper;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.entity.EventDate;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.service.EventDomainService;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final EventDomainService eventDomainService;
    private final EventMapper eventMapper;

    public EventResponse createEvent(CreateEventRequest request) {
        if (request.getEventDates() == null || request.getEventDates().isEmpty()) {
            throw new DomainValidationException("Event requires at least one session date");
        }

        Event event = eventMapper.toEntity(request);
        applyEventDates(event, request.getEventDates());
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request) {
        Event event = getEventEntity(eventId);
        eventMapper.updateEntityFromRequest(event, request);

        if (request.getEventDates() != null) {
            applyEventDates(event, request.getEventDates());
        }

        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    public EventResponse publishEvent(UUID eventId) {
        Event event = getEventEntity(eventId);
        if (event.getEventDates() == null || event.getEventDates().isEmpty()) {
            throw new DomainValidationException("Cannot publish an event without scheduled dates");
        }
        Event published = eventDomainService.publishEvent(event);
        return eventMapper.toResponse(published);
    }

    public EventResponse cancelEvent(UUID eventId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new DomainValidationException("Cancellation reason is required");
        }
        Event cancelled = eventDomainService.cancelEvent(eventId, reason);
        return eventMapper.toResponse(cancelled);
    }

    public EventResponse markFeatured(UUID eventId, boolean featured) {
        Event event = getEventEntity(eventId);
        event.setIsFeatured(featured);
        Event saved = eventRepository.save(event);
        return eventMapper.toResponse(saved);
    }

    public EventResponse getEvent(UUID eventId) {
        return eventMapper.toResponse(getEventEntity(eventId));
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> listEvents(EventStatus status, String searchTerm, Pageable pageable) {
        Page<Event> page;
        if (searchTerm != null && !searchTerm.isBlank()) {
            page = eventRepository.searchEvents(searchTerm.trim(), pageable);
        } else if (status != null) {
            page = eventRepository.findByStatus(status, pageable);
        } else {
            page = eventRepository.findAll(pageable);
        }
        return page.map(eventMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EventSummaryResponse> listPublishedSummaries(EventType type, Pageable pageable) {
        Page<Event> page;
        if (type != null) {
            page = eventRepository.findByEventTypeAndStatus(type, EventStatus.UPCOMING, pageable);
        } else {
            page = eventRepository.findByIsPublishedTrue(pageable);
        }
        return page.map(eventMapper::toSummaryResponse);
    }

    private Event getEventEntity(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + eventId));
    }

    private void applyEventDates(Event event, List<EventDateDto> eventDateDtos) {
        if (event.getEventDates() == null) {
            event.setEventDates(new ArrayList<>());
        } else {
            event.getEventDates().clear();
        }
        for (EventDateDto dto : eventDateDtos) {
            EventDate eventDate = EventDate.builder()
                    .event(event)
                    .sessionDate(dto.getSessionDate())
                    .sessionEndDate(dto.getSessionEndDate())
                    .sessionName(dto.getSessionName())
                    .sessionDescription(dto.getSessionDescription())
                    .instructorName(dto.getInstructorName())
                    .location(dto.getLocation())
                    .isOnline(dto.getIsOnline())
                    .meetingLink(dto.getMeetingLink())
                    .notes(dto.getNotes())
                    .build();
            event.addEventDate(eventDate);
        }
    }
}

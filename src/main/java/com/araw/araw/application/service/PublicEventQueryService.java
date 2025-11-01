package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.EventResponse;
import com.araw.araw.application.mapper.EventMapper;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.shared.exception.DomainNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicEventQueryService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventResponse getPublishedEvent(String slugOrId) {
        String identifier = slugOrId == null ? "" : slugOrId.trim();
        if (identifier.isEmpty()) {
            throw new DomainNotFoundException("Published event not found: " + slugOrId);
        }

        Event event = resolvePublishedEvent(identifier)
                .orElseThrow(() -> new DomainNotFoundException("Published event not found: " + identifier));

        if (!Boolean.TRUE.equals(event.getIsPublished())) {
            throw new DomainNotFoundException("Published event not found: " + identifier);
        }

        return eventMapper.toResponse(event);
    }

    private Optional<Event> resolvePublishedEvent(String identifier) {
        Optional<Event> bySlug = eventRepository.findByApplicationSlug(identifier)
                .filter(event -> Boolean.TRUE.equals(event.getIsPublished()));
        if (bySlug.isPresent()) {
            return bySlug;
        }

        return parseUuid(identifier)
                .flatMap(eventRepository::findById)
                .filter(event -> Boolean.TRUE.equals(event.getIsPublished()));
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}

package com.araw.araw.presentation;

import com.araw.araw.application.dto.event.CancelEventRequest;
import com.araw.araw.application.dto.event.CreateEventRequest;
import com.araw.araw.application.dto.event.EventResponse;
import com.araw.araw.application.dto.event.EventSummaryResponse;
import com.araw.araw.application.dto.event.UpdateEventRequest;
import com.araw.araw.application.service.EventApplicationService;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/araw/events")
@Validated
@RequiredArgsConstructor
public class EventController {

    private final EventApplicationService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @PutMapping("/{eventId}")
    public EventResponse updateEvent(@PathVariable UUID eventId,
                                     @Valid @RequestBody UpdateEventRequest request) {
        return eventService.updateEvent(eventId, request);
    }

    @PostMapping("/{eventId}/publish")
    public EventResponse publishEvent(@PathVariable UUID eventId) {
        return eventService.publishEvent(eventId);
    }

    @PostMapping("/{eventId}/complete")
    public EventResponse completeEvent(@PathVariable UUID eventId) {
        return eventService.completeEvent(eventId);
    }

    @PostMapping("/{eventId}/cancel")
    public EventResponse cancelEvent(@PathVariable UUID eventId,
                                     @Valid @RequestBody CancelEventRequest request) {
        return eventService.cancelEvent(eventId, request.getReason());
    }

    @PatchMapping("/{eventId}/featured")
    public EventResponse toggleFeatured(@PathVariable UUID eventId,
                                        @RequestParam("value") boolean featured) {
        return eventService.markFeatured(eventId, featured);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable UUID eventId) {
        return eventService.getEvent(eventId);
    }

    @GetMapping
    public PagedResponse<EventResponse> listEvents(
            @RequestParam(value = "status", required = false) EventStatus status,
            @RequestParam(value = "search", required = false) String search,
            Pageable pageable) {
        Page<EventResponse> page = eventService.listEvents(status, search, pageable);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/published")
    public PagedResponse<EventSummaryResponse> listPublished(
            @RequestParam(value = "type", required = false) EventType type,
            Pageable pageable) {
        Page<EventSummaryResponse> page = eventService.listPublishedSummaries(type, pageable);
        return PagedResponse.fromPage(page);
    }
}

package com.araw.araw.presentation;

import com.araw.araw.application.dto.event.CreateEventParticipantHighlightRequest;
import com.araw.araw.application.dto.event.EventParticipantHighlightDto;
import com.araw.araw.application.dto.event.UpdateEventParticipantHighlightRequest;
import com.araw.araw.application.service.EventParticipantHighlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/araw/events/{eventId}/participants/highlights")
@RequiredArgsConstructor
public class EventParticipantHighlightController {

    private final EventParticipantHighlightService highlightService;

    @GetMapping
    public List<EventParticipantHighlightDto> listHighlights(@PathVariable UUID eventId) {
        return highlightService.listHighlights(eventId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EventParticipantHighlightDto createHighlight(@PathVariable UUID eventId,
                                                        @Valid @RequestPart("payload") CreateEventParticipantHighlightRequest request,
                                                        @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return highlightService.createHighlight(eventId, request, photo);
    }

    @PutMapping(value = "/{highlightId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventParticipantHighlightDto updateHighlight(@PathVariable UUID eventId,
                                                        @PathVariable UUID highlightId,
                                                        @Valid @RequestPart("payload") UpdateEventParticipantHighlightRequest request,
                                                        @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return highlightService.updateHighlight(eventId, highlightId, request, photo);
    }

    @DeleteMapping("/{highlightId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHighlight(@PathVariable UUID eventId,
                                @PathVariable UUID highlightId) {
        highlightService.deleteHighlight(eventId, highlightId);
    }

    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderHighlights(@PathVariable UUID eventId,
                                  @RequestBody List<UUID> highlightOrder) {
        highlightService.reorderHighlights(eventId, highlightOrder);
    }
}

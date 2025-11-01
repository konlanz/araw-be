package com.araw.araw.presentation;

import com.araw.araw.application.dto.event.EventResponse;
import com.araw.araw.application.service.PublicEventQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final PublicEventQueryService publicEventQueryService;

    @GetMapping("/{slugOrId}")
    public EventResponse getPublishedEvent(@PathVariable String slugOrId) {
        return publicEventQueryService.getPublishedEvent(slugOrId);
    }
}

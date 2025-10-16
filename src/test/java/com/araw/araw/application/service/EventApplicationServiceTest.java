package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.CreateEventRequest;
import com.araw.araw.application.dto.event.EventDateDto;
import com.araw.araw.application.dto.event.EventResponse;
import com.araw.araw.application.dto.event.UpdateEventRequest;
import com.araw.araw.application.dto.event.LocationDto;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventApplicationServiceTest {

    @Autowired
    private EventApplicationService eventService;

    @Test
    void createAndPublishEvent() {
        CreateEventRequest request = CreateEventRequest.builder()
                .title("STEM Robotics Bootcamp")
                .description("Hands-on robotics bootcamp")
                .shortDescription("Robotics for teens")
                .eventType(EventType.WORKSHOP)
                .location(LocationDto.builder()
                        .venueName("ARAW Innovation Hub")
                        .addressLine1("1 Impact Way")
                        .city("Accra")
                        .stateProvince("Greater Accra")
                        .country("Ghana")
                        .isVirtual(false)
                        .isHybrid(false)
                        .build())
                .maxParticipants(40)
                .isFree(true)
                .eventDates(List.of(EventDateDto.builder()
                        .sessionDate(LocalDateTime.now().plusDays(7))
                        .sessionEndDate(LocalDateTime.now().plusDays(7).plusHours(3))
                        .sessionName("Day 1")
                        .build()))
                .build();

        EventResponse created = eventService.createEvent(request);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(EventStatus.DRAFT);

        EventResponse published = eventService.publishEvent(created.getId());
        assertThat(published.getStatus()).isEqualTo(EventStatus.UPCOMING);
        assertThat(published.getIsPublished()).isTrue();

        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .maxParticipants(50)
                .build();

        EventResponse updated = eventService.updateEvent(created.getId(), updateRequest);
        assertThat(updated.getMaxParticipants()).isEqualTo(50);
    }
}

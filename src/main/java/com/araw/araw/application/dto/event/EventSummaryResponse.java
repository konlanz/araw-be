package com.araw.araw.application.dto.event;

import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryResponse {
    private UUID id;
    private String title;
    private String shortDescription;
    private EventType eventType;
    private EventStatus status;
    private String city;
    private Boolean isVirtual;
    private LocalDateTime nextSessionDate;
    private Boolean isFree;
    private Double cost;
    private Integer availableSpots;
    private String thumbnailUrl;
    private Boolean isFeatured;
    private Boolean isRegistrationOpen;
}

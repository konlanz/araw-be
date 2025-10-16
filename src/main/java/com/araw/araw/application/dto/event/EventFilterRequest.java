package com.araw.araw.application.dto.event;

import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterRequest {
    private EventStatus status;
    private EventType eventType;
    private Boolean isVirtual;
    private Boolean isFree;
    private String city;
    private String state;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime minDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime maxDate;

    private Integer minAge;
    private Integer maxAge;
    private String searchTerm;
    private Boolean onlyFeatured;
    private Boolean onlyWithAvailableSpots;

    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}

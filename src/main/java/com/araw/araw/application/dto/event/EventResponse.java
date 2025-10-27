package com.araw.araw.application.dto.event;

import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.event.valueobject.EventType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private String shortDescription;
    private EventType eventType;
    private EventStatus status;
    private LocationDto location;
    private Integer maxParticipants;
    private Integer minAge;
    private Integer maxAge;
    private String applicationLink;
    private String applicationSlug;
    private LocalDateTime applicationLinkGeneratedAt;
    private LocalDateTime applicationDeadline;
    private LocalDateTime registrationOpensAt;
    private LocalDateTime registrationClosesAt;
    private Boolean isFree;
    private Double cost;
    private String currency;
    private Set<String> prerequisites;
    private Set<String> learningOutcomes;
    private Set<String> targetGrades;
    private List<EventDateDto> eventDates;
    private EventGalleryDto gallery;
    private String bannerImageUrl;
    private String thumbnailUrl;
    private Boolean isFeatured;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private Long viewCount;
    private Integer applicationCount;
    private Integer participantCount;
    private Boolean feedbackEnabled;
    private LocalDateTime feedbackOpensAt;
    private LocalDateTime feedbackClosesAt;
    private Integer availableSpots;
    private Boolean isRegistrationOpen;
    private Double capacityPercentage;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

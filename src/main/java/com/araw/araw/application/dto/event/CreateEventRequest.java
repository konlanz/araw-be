package com.araw.araw.application.dto.event;

import com.araw.araw.domain.event.valueobject.EventType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Event title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Location is required")
    private LocationDto location;

    @Min(value = 1, message = "Maximum participants must be at least 1")
    private Integer maxParticipants;

    @Min(value = 5, message = "Minimum age must be at least 5")
    @Max(value = 100, message = "Maximum age must not exceed 100")
    private Integer minAge;

    @Min(value = 5, message = "Maximum age must be at least 5")
    @Max(value = 100, message = "Maximum age must not exceed 100")
    private Integer maxAge;

    private String applicationLink;

    @Future(message = "Application deadline must be in the future")
    private LocalDateTime applicationDeadline;

    @Future(message = "Registration open date must be in the future")
    private LocalDateTime registrationOpensAt;

    @Future(message = "Registration close date must be in the future")
    private LocalDateTime registrationClosesAt;

    @NotNull(message = "Please specify if the event is free")
    private Boolean isFree;

    @Min(value = 0, message = "Cost cannot be negative")
    private Double cost;

    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a 3-letter code")
    private String currency = "USD";

    private Set<String> prerequisites;
    private Set<String> learningOutcomes;
    private Set<String> targetGrades;

    @NotEmpty(message = "Event must have at least one date")
    private List<EventDateDto> eventDates;

    private String bannerImageUrl;
    private String thumbnailUrl;

    private Boolean feedbackEnabled;
    private LocalDateTime feedbackOpensAt;
    private LocalDateTime feedbackClosesAt;
}

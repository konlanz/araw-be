package com.araw.araw.application.dto.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDateDto {
    private UUID id;

    @NotNull(message = "Session date is required")
    @Future(message = "Session date must be in the future")
    private LocalDateTime sessionDate;

    private LocalDateTime sessionEndDate;
    private String sessionName;
    private String sessionDescription;
    private String instructorName;
    private String location;
    private Boolean isOnline;
    private String meetingLink;
    private String notes;
}


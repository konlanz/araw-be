package com.araw.araw.application.dto.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventParticipantHighlightRequest {

    @NotNull(message = "Participant ID is required")
    private UUID participantId;

    @Size(max = 160, message = "Headline must not exceed 160 characters")
    private String headline;

    @Size(max = 2000, message = "Story must not exceed 2000 characters")
    private String story;

    private Boolean isFeatured;
    private Integer displayOrder;
}

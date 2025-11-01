package com.araw.araw.application.dto.event;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventParticipantHighlightRequest {

    @Size(max = 160, message = "Headline must not exceed 160 characters")
    private String headline;

    @Size(max = 2000, message = "Story must not exceed 2000 characters")
    private String story;

    private Boolean isFeatured;
    private Integer displayOrder;
}

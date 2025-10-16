package com.araw.araw.application.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackSummaryResponse {
    private UUID id;
    private UUID eventId;
    private String participantName;
    private Integer overallRating;
    private Double averageRating;
    private String comment;
    private LocalDateTime createdAt;
}

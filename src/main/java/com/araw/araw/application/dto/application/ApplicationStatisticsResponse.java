package com.araw.araw.application.dto.application;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsResponse {
    private UUID eventId;
    private String eventTitle;
    private Long totalApplications;
    private Map<String, Long> applicationsByStatus;
    private Double averageReviewScore;
    private Long pendingReview;
    private Long waitlistSize;
    private Map<String, Long> ageDistribution;
    private Map<String, Long> applicationSources;
    private Double acceptanceRate;
    private Double confirmationRate;
}

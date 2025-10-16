package com.araw.araw.application.dto.feedback;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatisticsResponse {
    private UUID eventId;
    private String eventTitle;
    private Long totalFeedbacks;
    private Double averageOverallRating;
    private Double averageContentRating;
    private Double averageInstructorRating;
    private Double averageOrganizationRating;
    private Double averageVenueRating;
    private Double averageValueRating;
    private Long recommendationCount;
    private Double recommendationPercentage;
    private Double netPromoterScore;
    private List<String> topSkillsGained;
    private Map<String, Double> aspectRatingsAverage;
    private Map<String, Integer> sentimentAnalysis;
    private Long testimonialCount;
    private Long featuredCount;
}
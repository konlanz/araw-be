package com.araw.araw.application.dto.feedback;

import com.araw.araw.domain.feedback.valueobject.FeedbackType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private UUID participantId;
    private String participantName;
    private String submittedByName;
    private String submittedByEmail;
    private FeedbackType feedbackType;
    private RatingDto rating;
    private Double averageRating;
    private String overallExperience;
    private String whatLearned;
    private String mostValuableAspect;
    private String improvementSuggestions;
    private Boolean wouldRecommend;
    private String recommendationReason;
    private Set<String> skillsGained;
    private Map<String, Integer> aspectRatings;
    private TestimonialDto testimonial;
    private Boolean isAnonymous;
    private Boolean consentToPublish;
    private Boolean isFeatured;
    private Boolean followUpCompleted;
    private String followUpNotes;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;
}

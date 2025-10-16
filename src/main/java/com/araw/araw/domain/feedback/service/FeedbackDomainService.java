package com.araw.araw.domain.feedback.service;

import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.feedback.entity.Feedback;
import com.araw.araw.domain.feedback.entity.Testimonial;
import com.araw.araw.domain.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackDomainService {

    private final FeedbackRepository feedbackRepository;


    public Testimonial createTestimonialFromFeedback(Feedback feedback, String quote) {
        if (!feedback.getConsentToPublish()) {
            throw new IllegalStateException("Cannot create testimonial without consent");
        }

        if (feedback.getRating() == null ||
                feedback.getRating().getOverallRating() == null ||
                feedback.getRating().getOverallRating() < 4) {
            throw new IllegalStateException("Testimonials require rating of 4 or higher");
        }

        String context = String.format("%s - %s",
                feedback.getEvent().getTitle(),
                feedback.getEvent().getEventType().getDisplayName()
        );

        feedback.createTestimonial(quote, context);
        return feedbackRepository.save(feedback).getTestimonial();
    }


    public Double calculateNPS(UUID eventId) {
        Object npsData = feedbackRepository.getNPSDataForEvent(eventId);

        if (npsData instanceof Object[] data) {
            Long promoters = (Long) data[0];
            Long passives = (Long) data[1];
            Long detractors = (Long) data[2];
            Long total = (Long) data[3];

            if (total == 0) return null;

            double promoterPercentage = (promoters * 100.0) / total;
            double detractorPercentage = (detractors * 100.0) / total;

            return promoterPercentage - detractorPercentage;
        }

        return null;
    }


    public Map<String, Integer> analyzeFeedbackSentiment(Event event) {
        List<Feedback> feedbacks = feedbackRepository.findByEventId(event.getId());

        int positive = 0;
        int neutral = 0;
        int negative = 0;

        for (Feedback feedback : feedbacks) {
            if (feedback.getRating() != null &&
                    feedback.getRating().getOverallRating() != null) {
                int rating = feedback.getRating().getOverallRating();

                if (rating >= 4 && Boolean.TRUE.equals(feedback.getWouldRecommend())) {
                    positive++;
                } else if (rating == 3) {
                    neutral++;
                } else {
                    negative++;
                }
            }
        }

        return Map.of(
                "positive", positive,
                "neutral", neutral,
                "negative", negative,
                "total", feedbacks.size()
        );
    }


    public EventFeedbackInsights getEventInsights(UUID eventId) {
        Double avgRating = feedbackRepository.getAverageRatingForEvent(eventId);
        Double avgContentRating = feedbackRepository.getAverageContentRatingForEvent(eventId);
        Double avgInstructorRating = feedbackRepository.getAverageInstructorRatingForEvent(eventId);
        Long recommendationCount = feedbackRepository.countRecommendationsForEvent(eventId);
        Double nps = calculateNPS(eventId);

        List<Object[]> topSkills = feedbackRepository.getTopSkillsGainedForEvent(eventId);
        List<String> topSkillsList = topSkills.stream()
                .limit(5)
                .map(arr -> (String) arr[0])
                .collect(Collectors.toList());

        return EventFeedbackInsights.builder()
                .eventId(eventId)
                .averageOverallRating(avgRating)
                .averageContentRating(avgContentRating)
                .averageInstructorRating(avgInstructorRating)
                .recommendationCount(recommendationCount)
                .netPromoterScore(nps)
                .topSkillsGained(topSkillsList)
                .build();
    }


    public List<Feedback> identifyFeedbackForFollowUp(Event event) {
        return feedbackRepository.findByEventId(event.getId()).stream()
                .filter(this::requiresFollowUp)
                .collect(Collectors.toList());
    }

    private boolean requiresFollowUp(Feedback feedback) {
        if (feedback.getRating() != null &&
                feedback.getRating().getOverallRating() != null &&
                feedback.getRating().getOverallRating() <= 2) {
            return true;
        }

        if (Boolean.FALSE.equals(feedback.getWouldRecommend())) {
            return true;
        }

        String[] concernKeywords = {"problem", "issue", "disappointed", "frustrated",
                "poor", "bad", "terrible", "worst"};

        String combinedText = String.join(" ",
                feedback.getOverallExperience() != null ? feedback.getOverallExperience() : "",
                feedback.getImprovementSuggestions() != null ? feedback.getImprovementSuggestions() : ""
        ).toLowerCase();

        for (String keyword : concernKeywords) {
            if (combinedText.contains(keyword)) {
                return true;
            }
        }

        return false;
    }


    public void autoFeatureFeedback() {
        List<Feedback> candidates = feedbackRepository
                .findByConsentToPublishTrueAndPublishedAtIsNotNull();

        candidates.stream()
                .filter(f -> f.getRating() != null &&
                        f.getRating().getOverallRating() != null &&
                        f.getRating().getOverallRating() >= 5)
                .filter(f -> f.getTestimonial() != null)
                .filter(f -> !f.getIsFeatured())
                .limit(10)
                .forEach(feedback -> {
                    feedback.feature();
                    feedbackRepository.save(feedback);
                });
    }

    @lombok.Builder
    @lombok.Getter
    public static class EventFeedbackInsights {
        private UUID eventId;
        private Double averageOverallRating;
        private Double averageContentRating;
        private Double averageInstructorRating;
        private Long recommendationCount;
        private Double netPromoterScore;
        private List<String> topSkillsGained;
    }
}


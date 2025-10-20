package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.CreateEventRequest;
import com.araw.araw.application.dto.event.EventDateDto;
import com.araw.araw.application.dto.event.LocationDto;
import com.araw.araw.application.dto.feedback.CreateFeedbackRequest;
import com.araw.araw.application.dto.feedback.FeedbackResponse;
import com.araw.araw.application.dto.feedback.RatingDto;
import com.araw.araw.domain.event.valueobject.EventType;
import com.araw.araw.domain.feedback.valueobject.FeedbackType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeedbackApplicationServiceTest {

    @Autowired
    private FeedbackApplicationService feedbackService;

    @Autowired
    private EventApplicationService eventService;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        CreateEventRequest request = CreateEventRequest.builder()
                .title("Community Showcase")
                .description("Showcase event")
                .shortDescription("Showcase")
                .eventType(EventType.SEMINAR)
                .location(LocationDto.builder()
                        .venueName("Community Hall")
                        .addressLine1("200 Unity Road")
                        .city("Accra")
                        .stateProvince("Greater Accra")
                        .country("Ghana")
                        .isVirtual(false)
                        .build())
                .isFree(true)
                .maxParticipants(100)
                .eventDates(List.of(EventDateDto.builder()
                        .sessionDate(LocalDateTime.now().plusDays(3))
                        .sessionEndDate(LocalDateTime.now().plusDays(3).plusHours(2))
                        .sessionName("Main Session")
                        .build()))
                .build();
        eventId = eventService.createEvent(request).getId();
    }

    @Test
    void createPublishAndFeatureFeedback() {
        CreateFeedbackRequest request = CreateFeedbackRequest.builder()
                .eventId(eventId)
                .submittedByName("Akosua")
                .submittedByEmail("akosua@example.com")
                .feedbackType(FeedbackType.POST_EVENT)
                .rating(RatingDto.builder()
                        .overallRating(5)
                        .contentRating(5)
                        .instructorRating(5)
                        .organizationRating(4)
                        .venueRating(4)
                        .valueRating(5)
                        .build())
                .overallExperience("Fantastic experience")
                .wouldRecommend(true)
                .consentToPublish(true)
                .build();

        FeedbackResponse created = feedbackService.createFeedback(request);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRating().getOverallRating()).isEqualTo(5);

        FeedbackResponse published = feedbackService.publishFeedback(created.getId());
        assertThat(published.getPublishedAt()).isNotNull();

        FeedbackResponse featured = feedbackService.featureFeedback(created.getId(), true);
        assertThat(featured.getIsFeatured()).isTrue();

        var insights = feedbackService.getEventInsights(eventId);
        assertThat(insights.getAverageOverallRating()).isNotNull();
    }
}

package com.araw.araw.application.service;

import com.araw.araw.application.dto.feedback.CreateFeedbackRequest;
import com.araw.araw.application.dto.feedback.CreateTestimonialRequest;
import com.araw.araw.application.dto.feedback.FeedbackResponse;
import com.araw.araw.application.dto.feedback.FeedbackSummaryResponse;
import com.araw.araw.application.dto.feedback.TestimonialDto;
import com.araw.araw.application.dto.feedback.UpdateFeedbackRequest;
import com.araw.araw.application.mapper.FeedbackMapper;
import com.araw.araw.domain.application.entity.Application;
import com.araw.araw.domain.application.repository.ApplicationRepository;
import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.feedback.entity.Feedback;
import com.araw.araw.domain.feedback.repository.FeedbackRepository;
import com.araw.araw.domain.feedback.service.FeedbackDomainService;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackApplicationService {

    private final FeedbackRepository feedbackRepository;
    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final FeedbackMapper feedbackMapper;
    private final FeedbackDomainService feedbackDomainService;
    private final ApplicationRepository applicationRepository;

    private static final java.util.EnumSet<ApplicationStatus> FEEDBACK_ELIGIBLE_STATUSES =
            java.util.EnumSet.of(ApplicationStatus.ACCEPTED, ApplicationStatus.CONFIRMED);

    public FeedbackResponse createFeedback(CreateFeedbackRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + request.getEventId()));

        if (!event.isFeedbackWindowOpen()) {
            throw new DomainValidationException("Feedback collection is not open for this event.");
        }

        Participant participant = null;
        if (request.getParticipantId() != null) {
            participant = participantRepository.findById(request.getParticipantId())
                    .orElseThrow(() -> new DomainNotFoundException("Participant not found: " + request.getParticipantId()));
            if (feedbackRepository.existsByEventIdAndParticipantId(event.getId(), participant.getId())) {
                throw new DomainValidationException("Participant has already submitted feedback for this event");
            }
        }

        if (participant == null && feedbackRepository.existsByEventIdAndSubmittedByEmail(event.getId(), request.getSubmittedByEmail())) {
            throw new DomainValidationException("Feedback already submitted with this email for the event");
        }

        ApplicationStatus applicationStatus = resolveApplicationStatus(event.getId(), participant, request.getSubmittedByEmail());
        if (applicationStatus == null || !FEEDBACK_ELIGIBLE_STATUSES.contains(applicationStatus)) {
            throw new DomainValidationException("Only accepted attendees may submit feedback for this event.");
        }

        Feedback feedback = feedbackMapper.toEntity(request);
        feedback.setEvent(event);
        feedback.setParticipant(participant);

        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    public FeedbackResponse updateFeedback(UUID feedbackId, UpdateFeedbackRequest request) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        feedbackMapper.updateEntity(feedback, request);
        if (request.getConsentToPublish() != null) {
            feedback.setConsentToPublish(request.getConsentToPublish());
        }
        if (request.getIsFeatured() != null) {
            if (request.getIsFeatured() && !Boolean.TRUE.equals(feedback.getConsentToPublish())) {
                throw new DomainValidationException("Cannot feature feedback without publication consent");
            }
            feedback.setIsFeatured(request.getIsFeatured());
        }
        if (request.getFollowUpCompleted() != null) {
            feedback.setFollowUpCompleted(request.getFollowUpCompleted());
        }
        if (request.getFollowUpNotes() != null) {
            feedback.setFollowUpNotes(request.getFollowUpNotes());
        }
        if (Boolean.TRUE.equals(request.getPublish())) {
            feedback.publish();
        } else if (Boolean.FALSE.equals(request.getPublish())) {
            feedback.setPublishedAt(null);
        }
        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    public FeedbackResponse publishFeedback(UUID feedbackId) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        feedback.publish();
        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    public FeedbackResponse featureFeedback(UUID feedbackId, boolean featured) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        if (featured && !Boolean.TRUE.equals(feedback.getConsentToPublish())) {
            throw new DomainValidationException("Cannot feature feedback without publication consent");
        }
        feedback.setIsFeatured(featured);
        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    public FeedbackResponse upsertTestimonial(UUID feedbackId, CreateTestimonialRequest request) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        validateTestimonialEligibility(feedback);

        String context = (request.getContext() != null && !request.getContext().isBlank())
                ? request.getContext().trim()
                : buildDefaultTestimonialContext(feedback);

        feedback.createTestimonial(request.getQuote().trim(), context);

        var testimonial = feedback.getTestimonial();
        if (request.getAuthorName() != null && !request.getAuthorName().isBlank()) {
            testimonial.setAuthorName(request.getAuthorName().trim());
        }
        testimonial.setAuthorTitle(request.getAuthorTitle());
        testimonial.setAuthorPhotoUrl(request.getAuthorPhotoUrl());
        testimonial.setHighlightText(request.getHighlightText());
        testimonial.setVideoTestimonialUrl(request.getVideoTestimonialUrl());
        testimonial.setDisplayOrder(request.getDisplayOrder());
        testimonial.setIsFeatured(Boolean.TRUE.equals(request.getIsFeatured()));
        testimonial.setIsPublished(request.getPublish() == null || Boolean.TRUE.equals(request.getPublish()));

        if (Boolean.TRUE.equals(request.getPublish())) {
            feedback.publish();
        }

        Feedback saved = feedbackRepository.save(feedback);
        return feedbackMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedback(UUID feedbackId) {
        return feedbackMapper.toResponse(getFeedbackEntity(feedbackId));
    }

    @Transactional(readOnly = true)
    public Page<FeedbackSummaryResponse> listFeedback(UUID eventId, Pageable pageable, String searchTerm) {
        Page<Feedback> page;
        if (searchTerm != null && !searchTerm.isBlank()) {
            page = feedbackRepository.searchFeedback(searchTerm.trim(), pageable);
        } else if (eventId != null) {
            page = feedbackRepository.findByEventId(eventId, pageable);
        } else {
            page = feedbackRepository.findAll(pageable);
        }
        return page.map(feedbackMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public FeedbackDomainService.EventFeedbackInsights getEventInsights(UUID eventId) {
        return feedbackDomainService.getEventInsights(eventId);
    }

    @Transactional(readOnly = true)
    public Page<TestimonialDto> listTestimonials(UUID eventId, Boolean featuredOnly, Pageable pageable) {
        Page<Feedback> page;
        boolean featured = Boolean.TRUE.equals(featuredOnly);
        if (featured) {
            if (eventId != null) {
                page = feedbackRepository.findFeaturedTestimonialsPage(eventId, pageable);
            } else {
                page = feedbackRepository.findFeaturedTestimonialsPage(pageable);
            }
        } else {
            if (eventId != null) {
                page = feedbackRepository.findTestimonialsByEvent(eventId, pageable);
            } else {
                page = feedbackRepository.findTestimonials(pageable);
            }
        }
        return page.map(feedbackMapper::toTestimonialDto);
    }

    public void deleteFeedback(UUID feedbackId) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        feedbackRepository.delete(feedback);
    }

    private Feedback getFeedbackEntity(UUID feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new DomainNotFoundException("Feedback not found: " + feedbackId));
    }

    private void validateTestimonialEligibility(Feedback feedback) {
        if (!Boolean.TRUE.equals(feedback.getConsentToPublish())) {
            throw new DomainValidationException("Cannot create testimonials without publication consent");
        }
        if (feedback.getRating() == null || feedback.getRating().getOverallRating() == null
                || feedback.getRating().getOverallRating() < 4) {
            throw new DomainValidationException("Testimonials require an overall rating of at least 4");
        }
    }

    private String buildDefaultTestimonialContext(Feedback feedback) {
        String eventTitle = feedback.getEvent() != null ? feedback.getEvent().getTitle() : "";
        String type = feedback.getEvent() != null && feedback.getEvent().getEventType() != null
                ? feedback.getEvent().getEventType().getDisplayName()
                : "Event";
        return (eventTitle != null && !eventTitle.isBlank())
                ? "%s - %s".formatted(eventTitle, type)
                : type;
    }

    private ApplicationStatus resolveApplicationStatus(UUID eventId, Participant participant, String email) {
        if (participant != null) {
            return applicationRepository.findTopByEventIdAndParticipantIdOrderBySubmittedAtDesc(eventId, participant.getId())
                    .map(Application::getStatus)
                    .orElse(null);
        }
        if (email == null || email.isBlank()) {
            return null;
        }
        return applicationRepository.findTopByEventIdAndEmailOrderBySubmittedAtDesc(eventId, email.trim())
                .map(Application::getStatus)
                .orElse(null);
    }
}

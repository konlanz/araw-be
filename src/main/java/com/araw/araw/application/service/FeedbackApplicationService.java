package com.araw.araw.application.service;

import com.araw.araw.application.dto.feedback.CreateFeedbackRequest;
import com.araw.araw.application.dto.feedback.FeedbackResponse;
import com.araw.araw.application.dto.feedback.FeedbackSummaryResponse;
import com.araw.araw.application.dto.feedback.UpdateFeedbackRequest;
import com.araw.araw.application.mapper.FeedbackMapper;
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

    public FeedbackResponse createFeedback(CreateFeedbackRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + request.getEventId()));

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

    public void deleteFeedback(UUID feedbackId) {
        Feedback feedback = getFeedbackEntity(feedbackId);
        feedbackRepository.delete(feedback);
    }

    private Feedback getFeedbackEntity(UUID feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new DomainNotFoundException("Feedback not found: " + feedbackId));
    }
}

package com.araw.araw.application.service;

import com.araw.araw.application.dto.event.CreateEventParticipantHighlightRequest;
import com.araw.araw.application.dto.event.EventParticipantHighlightDto;
import com.araw.araw.application.dto.event.UpdateEventParticipantHighlightRequest;
import com.araw.araw.application.mapper.EventMapper;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.entity.EventParticipantHighlight;
import com.araw.araw.domain.event.repository.EventParticipantHighlightRepository;
import com.araw.araw.domain.event.repository.EventRepository;
import com.araw.araw.domain.event.valueobject.EventStatus;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.repository.ParticipantRepository;
import com.araw.media.application.MediaStorageService;
import com.araw.media.application.command.MediaUploadCommand;
import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventParticipantHighlightService {

    private static final long MAX_PHOTO_SIZE_BYTES = 25L * 1024 * 1024; // 25 MB

    private final EventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final EventParticipantHighlightRepository highlightRepository;
    private final MediaStorageService mediaStorageService;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public List<EventParticipantHighlightDto> listHighlights(UUID eventId) {
        List<EventParticipantHighlight> highlights = highlightRepository
                .findByEventIdOrderByDisplayOrderAscCreatedAtDesc(eventId);
        List<EventParticipantHighlightDto> dtos = eventMapper.toEventParticipantHighlightDtoList(highlights);
        populateMediaUrls(dtos);
        return dtos;
    }

    public EventParticipantHighlightDto createHighlight(UUID eventId,
                                                        CreateEventParticipantHighlightRequest request,
                                                        MultipartFile photo) {
        Event event = requireCompletedEvent(eventId);
        Participant participant = getParticipant(request.getParticipantId());

        if (highlightRepository.findByEventIdAndParticipantId(eventId, participant.getId()).isPresent()) {
            throw new DomainValidationException("Participant is already featured for this event");
        }

        EventParticipantHighlight highlight = EventParticipantHighlight.builder()
                .event(event)
                .participant(participant)
                .headline(request.getHeadline())
                .story(request.getStory())
                .isFeatured(request.getIsFeatured() == null || request.getIsFeatured())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : nextDisplayOrder(eventId))
                .build();

        if (photo != null && !photo.isEmpty()) {
            highlight = attachPhoto(highlight, photo);
        }

        EventParticipantHighlight saved = highlightRepository.save(highlight);
        EventParticipantHighlightDto dto = eventMapper.toEventParticipantHighlightDto(saved);
        populateMediaUrl(dto);
        return dto;
    }

    public EventParticipantHighlightDto updateHighlight(UUID eventId,
                                                        UUID highlightId,
                                                        UpdateEventParticipantHighlightRequest request,
                                                        MultipartFile photo) {
        Event event = getEvent(eventId);
        EventParticipantHighlight highlight = getHighlight(highlightId);
        ensureHighlightBelongsToEvent(highlight, event);

        if (request.getHeadline() != null) {
            highlight.setHeadline(request.getHeadline());
        }
        if (request.getStory() != null) {
            highlight.setStory(request.getStory());
        }
        if (request.getIsFeatured() != null) {
            highlight.setIsFeatured(request.getIsFeatured());
        }
        if (request.getDisplayOrder() != null) {
            highlight.setDisplayOrder(request.getDisplayOrder());
        }

        if (photo != null && !photo.isEmpty()) {
            removePhoto(highlight);
            highlight = attachPhoto(highlight, photo);
        }

        EventParticipantHighlight saved = highlightRepository.save(highlight);
        EventParticipantHighlightDto dto = eventMapper.toEventParticipantHighlightDto(saved);
        populateMediaUrl(dto);
        return dto;
    }

    public void deleteHighlight(UUID eventId, UUID highlightId) {
        Event event = getEvent(eventId);
        EventParticipantHighlight highlight = getHighlight(highlightId);
        ensureHighlightBelongsToEvent(highlight, event);
        removePhoto(highlight);
        highlightRepository.delete(highlight);
    }

    public void reorderHighlights(UUID eventId, List<UUID> orderedIds) {
        Event event = getEvent(eventId);
        List<EventParticipantHighlight> highlights = highlightRepository
                .findByEventIdOrderByDisplayOrderAscCreatedAtDesc(event.getId());
        int index = 0;
        for (UUID id : orderedIds) {
            for (EventParticipantHighlight highlight : highlights) {
                if (highlight.getId().equals(id)) {
                    highlight.setDisplayOrder(index++);
                }
            }
        }
        highlights.sort(Comparator.comparing(EventParticipantHighlight::getDisplayOrder));
        highlightRepository.saveAll(highlights);
    }

    public void populateMediaUrls(List<EventParticipantHighlightDto> highlights) {
        if (highlights == null) {
            return;
        }
        highlights.forEach(this::populateMediaUrl);
    }

    private EventParticipantHighlight attachPhoto(EventParticipantHighlight highlight, MultipartFile photo) {
        if (photo.getSize() > MAX_PHOTO_SIZE_BYTES) {
            throw new DomainValidationException("Highlight photo exceeds the maximum size of 25 MB");
        }
        MediaAsset asset = storeMedia(photo, MediaCategory.EVENT_MEDIA,
                "Participant spotlight for event " + highlight.getEvent().getTitle());
        highlight.setMediaAssetId(asset.getId());
        highlight.setPhotoUrl(asset.getObjectKey());
        return highlight;
    }

    private void removePhoto(EventParticipantHighlight highlight) {
        if (highlight.getMediaAssetId() != null) {
            try {
                mediaStorageService.deleteAsset(highlight.getMediaAssetId());
            } catch (Exception ex) {
                log.warn("Failed to delete highlight photo asset {}", highlight.getMediaAssetId(), ex);
            }
            highlight.setMediaAssetId(null);
            highlight.setPhotoUrl(null);
        }
    }

    private MediaAsset storeMedia(MultipartFile file, MediaCategory category, String description) {
        try (MediaUploadCommand command = new MediaUploadCommand(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream(),
                category,
                description
        )) {
            return mediaStorageService.storeMedia(command);
        } catch (IOException ex) {
            throw new DomainValidationException("Failed to read uploaded photo", ex);
        }
    }

    private Event requireCompletedEvent(UUID eventId) {
        Event event = getEvent(eventId);
        if (event.getStatus() != EventStatus.COMPLETED) {
            throw new DomainValidationException("Participant highlights can only be added after the event is completed");
        }
        return event;
    }

    private Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new DomainNotFoundException("Event not found: " + eventId));
    }

    private Participant getParticipant(UUID participantId) {
        if (participantId == null) {
            throw new DomainValidationException("Participant ID is required");
        }
        return participantRepository.findById(participantId)
                .orElseThrow(() -> new DomainNotFoundException("Participant not found: " + participantId));
    }

    private EventParticipantHighlight getHighlight(UUID highlightId) {
        return highlightRepository.findById(highlightId)
                .orElseThrow(() -> new DomainNotFoundException("Participant highlight not found: " + highlightId));
    }

    private void ensureHighlightBelongsToEvent(EventParticipantHighlight highlight, Event event) {
        if (!highlight.getEvent().getId().equals(event.getId())) {
            throw new DomainValidationException("Highlight does not belong to the specified event");
        }
    }

    private int nextDisplayOrder(UUID eventId) {
        return highlightRepository.findByEventIdOrderByDisplayOrderAscCreatedAtDesc(eventId)
                .stream()
                .map(EventParticipantHighlight::getDisplayOrder)
                .max(Integer::compareTo)
                .map(val -> val + 1)
                .orElse(0);
    }

    private void populateMediaUrl(EventParticipantHighlightDto dto) {
        if (dto == null) {
            return;
        }
        if (dto.getMediaAssetId() != null) {
            try {
                dto.setDownloadUrl(mediaStorageService.generatePresignedUrl(dto.getMediaAssetId()));
            } catch (Exception ex) {
                log.warn("Failed to generate presigned URL for participant highlight {}", dto.getMediaAssetId(), ex);
            }
        } else if (dto.getPhotoUrl() != null) {
            dto.setDownloadUrl(dto.getPhotoUrl());
        }
    }
}

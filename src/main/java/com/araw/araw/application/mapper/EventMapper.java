package com.araw.araw.application.mapper;

import com.araw.araw.application.dto.event.*;
import com.araw.araw.domain.event.entity.Event;
import com.araw.araw.domain.event.entity.EventDate;
import com.araw.araw.domain.event.entity.EventGallery;
import com.araw.araw.domain.event.entity.EventParticipantHighlight;
import com.araw.araw.domain.event.entity.GalleryImage;
import com.araw.araw.domain.event.entity.GalleryVideo;
import com.araw.araw.domain.event.valueobject.Location;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface EventMapper {

    @Mapping(target = "location", source = "location")
    @Mapping(target = "eventDates", source = "eventDates")
    @Mapping(target = "gallery", source = "gallery")
    @Mapping(target = "participantHighlights", source = "participantHighlights")
    @Mapping(target = "availableSpots", expression = "java(calculateAvailableSpots(event))")
    @Mapping(target = "isRegistrationOpen", expression = "java(event.isRegistrationOpen())")
    @Mapping(target = "capacityPercentage", expression = "java(calculateCapacityPercentage(event))")
    @Mapping(target = "createdByUsername", expression = "java(event.getCreatedBy() != null ? event.getCreatedBy().getUsername() : null)")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    // Entity to Summary Response
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "shortDescription", source = "shortDescription")
    EventSummaryResponse toSummaryResponse(Event event);

    List<EventSummaryResponse> toSummaryResponseList(List<Event> events);

    // Request to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(com.araw.araw.domain.event.valueobject.EventStatus.DRAFT)")
    @Mapping(target = "location", source = "location")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "eventDates", ignore = true)
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "isFeatured", constant = "false")
    @Mapping(target = "isPublished", constant = "false")
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "applicationCount", constant = "0")
    @Mapping(target = "participantCount", constant = "0")
    @Mapping(target = "applicationSlug", ignore = true)
    @Mapping(target = "applicationLinkGeneratedAt", ignore = true)
    @Mapping(target = "feedbackEnabled", expression = "java(request.getFeedbackEnabled() != null ? request.getFeedbackEnabled() : Boolean.FALSE)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Event toEntity(CreateEventRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "eventDates", ignore = true)
    @Mapping(target = "gallery", ignore = true)
    @Mapping(target = "isFeatured", ignore = true)
    @Mapping(target = "isPublished", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "applicationCount", ignore = true)
    @Mapping(target = "participantCount", ignore = true)
    @Mapping(target = "applicationSlug", ignore = true)
    @Mapping(target = "applicationLinkGeneratedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(@MappingTarget Event event, UpdateEventRequest request);

    LocationDto toLocationDto(Location location);
    Location toLocation(LocationDto dto);

    EventDateDto toEventDateDto(EventDate eventDate);
    List<EventDateDto> toEventDateDtoList(List<EventDate> eventDates);

    EventGalleryDto toEventGalleryDto(EventGallery gallery);
    GalleryImageDto toGalleryImageDto(GalleryImage image);
    List<GalleryImageDto> toGalleryImageDtoList(List<GalleryImage> images);
    GalleryVideoDto toGalleryVideoDto(GalleryVideo video);
    List<GalleryVideoDto> toGalleryVideoDtoList(List<GalleryVideo> videos);
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "participantName", expression = "java(highlight.getParticipant() != null ? highlight.getParticipant().getFullName() : null)")
    EventParticipantHighlightDto toEventParticipantHighlightDto(EventParticipantHighlight highlight);
    List<EventParticipantHighlightDto> toEventParticipantHighlightDtoList(List<EventParticipantHighlight> highlights);

    default Integer calculateAvailableSpots(Event event) {
        if (event.getMaxParticipants() == null) {
            return null;
        }
        return event.getMaxParticipants() - event.getParticipantCount();
    }

    default Double calculateCapacityPercentage(Event event) {
        if (event.getMaxParticipants() == null || event.getMaxParticipants() == 0) {
            return null;
        }
        return (double) event.getParticipantCount() / event.getMaxParticipants() * 100;
    }
}

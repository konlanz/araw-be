package com.araw.araw.application.mapper;

import com.araw.araw.application.dto.feedback.CreateFeedbackRequest;
import com.araw.araw.application.dto.feedback.FeedbackResponse;
import com.araw.araw.application.dto.feedback.FeedbackSummaryResponse;
import com.araw.araw.application.dto.feedback.RatingDto;
import com.araw.araw.application.dto.feedback.TestimonialDto;
import com.araw.araw.application.dto.feedback.UpdateFeedbackRequest;
import com.araw.araw.domain.feedback.entity.Feedback;
import com.araw.araw.domain.feedback.valueobject.Rating;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FeedbackMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventTitle", source = "event.title")
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "participantName", expression = "java(getParticipantName(feedback))")
    @Mapping(target = "rating", expression = "java(toRatingDto(feedback.getRating()))")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(feedback.getRating()))")
    @Mapping(target = "testimonial", expression = "java(toTestimonialDto(feedback))")
    FeedbackResponse toResponse(Feedback feedback);

    List<FeedbackResponse> toResponseList(List<Feedback> feedbacks);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "participantName", expression = "java(getParticipantName(feedback))")
    @Mapping(target = "overallRating", expression = "java(feedback.getRating() != null ? feedback.getRating().getOverallRating() : null)")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(feedback.getRating()))")
    FeedbackSummaryResponse toSummaryResponse(Feedback feedback);

    List<FeedbackSummaryResponse> toSummaryResponseList(List<Feedback> feedbacks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "rating", expression = "java(createRating(request.getRating()))")
    @Mapping(target = "testimonial", ignore = true)
    @Mapping(target = "skillsGained", source = "skillsGained")
    @Mapping(target = "aspectRatings", source = "aspectRatings")
    @Mapping(target = "isAnonymous", expression = "java(Boolean.TRUE.equals(request.getIsAnonymous()))")
    @Mapping(target = "consentToPublish", expression = "java(Boolean.TRUE.equals(request.getConsentToPublish()))")
    @Mapping(target = "isFeatured", constant = "false")
    @Mapping(target = "followUpCompleted", constant = "false")
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Feedback toEntity(CreateFeedbackRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "participant", ignore = true)
    @Mapping(target = "rating", expression = "java(updateRating(feedback.getRating(), request.getRating()))")
    @Mapping(target = "testimonial", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void updateEntity(@MappingTarget Feedback feedback, UpdateFeedbackRequest request);

    default String getParticipantName(Feedback feedback) {
        if (feedback.getIsAnonymous()) {
            return "Anonymous";
        }
        if (feedback.getParticipant() != null) {
            return feedback.getParticipant().getFirstName() + " " +
                    feedback.getParticipant().getLastName();
        }
        return feedback.getSubmittedByName();
    }

    default Rating createRating(RatingDto dto) {
        if (dto == null) {
            return null;
        }
        return Rating.builder()
                .overallRating(dto.getOverallRating())
                .contentRating(dto.getContentRating())
                .instructorRating(dto.getInstructorRating())
                .organizationRating(dto.getOrganizationRating())
                .venueRating(dto.getVenueRating())
                .valueRating(dto.getValueRating())
                .build();
    }

    default Rating updateRating(Rating existing, RatingDto dto) {
        if (existing == null) {
            existing = new Rating();
        }
        if (dto == null) {
            return existing;
        }
        if (dto.getOverallRating() != null) {
            existing.setOverallRating(dto.getOverallRating());
        }
        if (dto.getContentRating() != null) {
            existing.setContentRating(dto.getContentRating());
        }
        if (dto.getInstructorRating() != null) {
            existing.setInstructorRating(dto.getInstructorRating());
        }
        if (dto.getOrganizationRating() != null) {
            existing.setOrganizationRating(dto.getOrganizationRating());
        }
        if (dto.getVenueRating() != null) {
            existing.setVenueRating(dto.getVenueRating());
        }
        if (dto.getValueRating() != null) {
            existing.setValueRating(dto.getValueRating());
        }
        return existing;
    }

    default Double calculateAverageRating(Rating rating) {
        if (rating == null) {
            return null;
        }

        int total = 0;
        int count = 0;

        if (rating.getOverallRating() != null) {
            total += rating.getOverallRating();
            count++;
        }
        if (rating.getContentRating() != null) {
            total += rating.getContentRating();
            count++;
        }
        if (rating.getInstructorRating() != null) {
            total += rating.getInstructorRating();
            count++;
        }
        if (rating.getVenueRating() != null) {
            total += rating.getVenueRating();
            count++;
        }
        if (rating.getOrganizationRating() != null) {
            total += rating.getOrganizationRating();
            count++;
        }
        if (rating.getValueRating() != null) {
            total += rating.getValueRating();
            count++;
        }

        return count > 0 ? (double) total / count : null;
    }

    default RatingDto toRatingDto(Rating rating) {
        if (rating == null) {
            return null;
        }
        return RatingDto.builder()
                .overallRating(rating.getOverallRating())
                .contentRating(rating.getContentRating())
                .instructorRating(rating.getInstructorRating())
                .organizationRating(rating.getOrganizationRating())
                .venueRating(rating.getVenueRating())
                .valueRating(rating.getValueRating())
                .build();
    }

    default TestimonialDto toTestimonialDto(Feedback feedback) {
        if (feedback.getTestimonial() == null) {
            return null;
        }
        var testimonial = feedback.getTestimonial();
        return TestimonialDto.builder()
                .id(testimonial.getId())
                .quote(testimonial.getQuote())
                .context(testimonial.getContext())
                .authorName(testimonial.getAuthorName())
                .authorTitle(testimonial.getAuthorTitle())
                .authorPhotoUrl(testimonial.getAuthorPhotoUrl())
                .highlightText(testimonial.getHighlightText())
                .videoTestimonialUrl(testimonial.getVideoTestimonialUrl())
                .isFeatured(testimonial.getIsFeatured())
                .displayOrder(testimonial.getDisplayOrder())
                .isPublished(testimonial.getIsPublished())
                .createdAt(testimonial.getCreatedAt())
                .build();
    }
}

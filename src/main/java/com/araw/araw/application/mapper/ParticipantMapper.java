package com.araw.araw.application.mapper;

import com.araw.araw.application.dto.participant.AchievementDto;
import com.araw.araw.application.dto.participant.ContactInfoDto;
import com.araw.araw.application.dto.participant.CreateParticipantRequest;
import com.araw.araw.application.dto.participant.ParticipantProgressDto;
import com.araw.araw.application.dto.participant.ParticipantResponse;
import com.araw.araw.application.dto.participant.ParticipantSummaryResponse;
import com.araw.araw.application.dto.participant.UpdateParticipantRequest;
import com.araw.araw.domain.application.valueobject.ContactInfo;
import com.araw.araw.domain.participant.enitity.Participant;
import com.araw.araw.domain.participant.enitity.ParticipantProgress;
import com.araw.araw.domain.participant.valueobject.Achievement;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ParticipantMapper {

    @Mapping(target = "age", expression = "java(calculateAge(participant.getDateOfBirth()))")
    @Mapping(target = "achievements", source = "achievements")
    @Mapping(target = "progressRecords", source = "progressRecords")
    @Mapping(target = "milestones", expression = "java(extractMilestones(participant))")
    @Mapping(target = "engagementScore", expression = "java(calculateEngagementScore(participant))")
    ParticipantResponse toResponse(Participant participant);

    List<ParticipantResponse> toResponseList(List<Participant> participants);

    @Mapping(target = "age", expression = "java(calculateAge(participant.getDateOfBirth()))")
    @Mapping(target = "email", source = "contactInfo.email")
    @Mapping(target = "grade", source = "gradeLevel")
    ParticipantSummaryResponse toSummaryResponse(Participant participant);

    List<ParticipantSummaryResponse> toSummaryResponseList(List<Participant> participants);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attendedEvents", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "progressRecords", ignore = true)
    @Mapping(target = "achievements", ignore = true)
    @Mapping(target = "totalEventsAttended", expression = "java(defaultInteger(request.getTotalEventsAttended()))")
    @Mapping(target = "totalHoursParticipated", expression = "java(defaultInteger(request.getTotalHoursParticipated()))")
    @Mapping(target = "isAlumni", expression = "java(defaultBooleanWithFallback(request.getAlumni(), false))")
    @Mapping(target = "isFeaturedAlumni", expression = "java(defaultBooleanWithFallback(request.getFeaturedAlumni(), false))")
    @Mapping(target = "consentForCommunication", expression = "java(defaultBooleanWithFallback(request.getConsentForCommunication(), true))")
    @Mapping(target = "consentForPhotos", expression = "java(defaultBooleanWithFallback(request.getConsentForPhotos(), true))")
    @Mapping(target = "consentForTestimonials", expression = "java(defaultBooleanWithFallback(request.getConsentForTestimonials(), true))")
    @Mapping(target = "contactInfo", expression = "java(toContactInfo(request.getContactInfo()))")
    Participant toEntity(CreateParticipantRequest request);

    // Update Entity from Request
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attendedEvents", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "progressRecords", ignore = true)
    @Mapping(target = "achievements", ignore = true)
    @Mapping(target = "contactInfo", expression = "java(updateContactInfo(participant.getContactInfo(), request.getContactInfo()))")
    void updateEntity(@MappingTarget Participant participant, UpdateParticipantRequest request);

    // Helper method
    default Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    default ContactInfo toContactInfo(ContactInfoDto dto) {
        if (dto == null) {
            return null;
        }
        return ContactInfo.builder()
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .alternativePhone(dto.getAlternativePhone())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .stateProvince(dto.getStateProvince())
                .postalCode(dto.getPostalCode())
                .country(dto.getCountry())
                .preferredContactMethod(dto.getPreferredContactMethod())
                .bestTimeToContact(dto.getBestTimeToContact())
                .build();
    }

    default ContactInfo updateContactInfo(ContactInfo existing, ContactInfoDto dto) {
        if (dto == null) {
            return existing;
        }
        ContactInfo target = existing != null ? existing : new ContactInfo();
        if (dto.getEmail() != null) target.setEmail(dto.getEmail());
        if (dto.getPhoneNumber() != null) target.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getAlternativePhone() != null) target.setAlternativePhone(dto.getAlternativePhone());
        if (dto.getAddressLine1() != null) target.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) target.setAddressLine2(dto.getAddressLine2());
        if (dto.getCity() != null) target.setCity(dto.getCity());
        if (dto.getStateProvince() != null) target.setStateProvince(dto.getStateProvince());
        if (dto.getPostalCode() != null) target.setPostalCode(dto.getPostalCode());
        if (dto.getCountry() != null) target.setCountry(dto.getCountry());
        if (dto.getPreferredContactMethod() != null) target.setPreferredContactMethod(dto.getPreferredContactMethod());
        if (dto.getBestTimeToContact() != null) target.setBestTimeToContact(dto.getBestTimeToContact());
        return target;
    }

    default java.util.List<AchievementDto> toAchievementDtoList(java.util.List<Achievement> achievements) {
        if (achievements == null) {
            return java.util.Collections.emptyList();
        }
        return achievements.stream()
                .map(this::toAchievementDto)
                .collect(Collectors.toList());
    }

    default AchievementDto toAchievementDto(Achievement achievement) {
        if (achievement == null) {
            return null;
        }
        Double percentage = null;
        if (achievement.getScore() != null && achievement.getMaxScore() != null && achievement.getMaxScore() > 0) {
            percentage = achievement.getScore() * 100.0 / achievement.getMaxScore();
        }
        return AchievementDto.builder()
                .id(achievement.getId())
                .achievementCode(achievement.getAchievementCode())
                .title(achievement.getTitle())
                .description(achievement.getDescription())
                .achievementType(achievement.getAchievementType())
                .achievementLevel(achievement.getAchievementLevel())
                .category(achievement.getCategory())
                .earnedDate(achievement.getEarnedDate())
                .expiryDate(achievement.getExpiryDate())
                .badgeImageUrl(achievement.getBadgeImageUrl())
                .certificateUrl(achievement.getCertificateUrl())
                .certificateNumber(achievement.getCertificateNumber())
                .skillsDemonstrated(achievement.getSkillsDemonstrated())
                .score(achievement.getScore())
                .maxScore(achievement.getMaxScore())
                .percentage(percentage)
                .percentile(achievement.getPercentile())
                .isVerified(achievement.getIsVerified())
                .isValid(achievement.isValid())
                .isFeatured(achievement.getIsFeatured())
                .build();
    }

    default java.util.List<ParticipantProgressDto> toProgressDtoList(java.util.List<ParticipantProgress> progressRecords) {
        if (progressRecords == null) {
            return java.util.Collections.emptyList();
        }
        return progressRecords.stream()
                .map(this::toProgressDto)
                .collect(Collectors.toList());
    }

    default ParticipantProgressDto toProgressDto(ParticipantProgress progress) {
        if (progress == null) {
            return null;
        }
        return ParticipantProgressDto.builder()
                .id(progress.getId())
                .eventId(progress.getEvent() != null ? progress.getEvent().getId() : null)
                .eventTitle(progress.getEvent() != null ? progress.getEvent().getTitle() : null)
                .progressType(progress.getProgressType())
                .title(progress.getTitle())
                .description(progress.getDescription())
                .achievementDate(progress.getAchievementDate())
                .recordedBy(progress.getRecordedBy())
                .build();
    }

    default List<String> extractMilestones(Participant participant) {
        if (participant.getAchievements() == null) {
            return List.of();
        }
        return participant.getAchievements().stream()
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    if (a.getEarnedDate() == null || b.getEarnedDate() == null) {
                        return 0;
                    }
                    return a.getEarnedDate().compareTo(b.getEarnedDate());
                })
                .map(Achievement::getTitle)
                .collect(Collectors.toList());
    }

    default Double calculateEngagementScore(Participant participant) {
        int events = participant.getTotalEventsAttended() != null ? participant.getTotalEventsAttended() : 0;
        int achievements = participant.getAchievements() != null ? participant.getAchievements().size() : 0;
        int hours = participant.getTotalHoursParticipated() != null ? participant.getTotalHoursParticipated() : 0;
        if (events == 0 && achievements == 0 && hours == 0) {
            return 0.0;
        }
        return events * 5.0 + achievements * 10.0 + hours * 0.5;
    }

    default Integer defaultInteger(Integer value) {
        return value != null ? value : 0;
    }

    default Boolean defaultBooleanWithFallback(Boolean value, boolean fallback) {
        return value != null ? value : fallback;
    }
}

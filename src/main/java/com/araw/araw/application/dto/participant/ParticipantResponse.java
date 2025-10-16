package com.araw.araw.application.dto.participant;

import com.araw.araw.domain.application.valueobject.EducationLevel;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private UUID id;
    private String participantCode;
    private String firstName;
    private String lastName;
    private String preferredName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private ContactInfoDto contactInfo;
    private EducationLevel educationLevel;
    private String schoolName;
    private String gradeLevel;
    private Integer graduationYear;
    private Set<String> interests;
    private Set<String> skills;
    private Integer totalEventsAttended;
    private Integer totalHoursParticipated;
    private Boolean isAlumni;
    private String alumniStory;
    private String currentStatus;
    private String currentInstitution;
    private String currentField;
    private String linkedinUrl;
    private String portfolioUrl;
    private String profilePictureUrl;
    private Boolean isFeaturedAlumni;
    private List<AchievementDto> achievements;
    private List<ParticipantProgressDto> progressRecords;
    private Double engagementScore;
    private List<String> milestones;
    private LocalDateTime joinedAt;
}

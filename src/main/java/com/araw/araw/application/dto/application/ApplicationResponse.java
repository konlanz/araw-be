package com.araw.araw.application.dto.application;

import com.araw.araw.domain.application.valueobject.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private UUID id;
    private String applicationNumber;
    private UUID eventId;
    private String eventTitle;
    private UUID participantId;
    private ApplicantInfoDto applicantInfo;
    private String email;
    private ApplicationStatus status;
    private String motivationStatement;
    private String priorExperience;
    private String learningGoals;
    private String linkedinProfileUrl;
    private Map<String, String> customAnswers;
    private List<ApplicationDocumentDto> documents;

    private Boolean guardianConsent;
    private String guardianName;
    private String guardianEmail;
    private String guardianPhone;

    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;

    private Set<String> dietaryRestrictions;
    private Set<String> medicalConditions;
    private String specialAccommodations;

    private Integer reviewScore;
    private String reviewNotes;
    private String reviewedBy;
    private LocalDateTime reviewedAt;

    private LocalDateTime acceptanceSentAt;
    private String rejectionReason;
    private Integer waitlistPosition;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    private String source;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}

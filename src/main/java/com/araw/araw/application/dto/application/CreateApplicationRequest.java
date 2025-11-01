package com.araw.araw.application.dto.application;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {

    private UUID eventId;

    private UUID participantId;

    @NotNull(message = "Applicant information is required")
    private ApplicantInfoDto applicantInfo;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 2000, message = "Motivation statement must not exceed 2000 characters")
    private String motivationStatement;

    @Size(max = 2000, message = "Prior experience must not exceed 2000 characters")
    private String priorExperience;

    @Size(max = 1000, message = "Learning goals must not exceed 1000 characters")
    private String learningGoals;

    @Size(max = 500, message = "LinkedIn profile URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)?linkedin\\.com/.*$", message = "LinkedIn profile must be a valid linkedin.com URL")
    private String linkedinProfileUrl;

    private Map<String, String> customAnswers;

    private Boolean guardianConsent;
    private String guardianName;

    @Email(message = "Invalid guardian email format")
    private String guardianEmail;

    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number")
    private String guardianPhone;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid emergency phone number")
    private String emergencyContactPhone;

    @NotBlank(message = "Emergency contact relation is required")
    private String emergencyContactRelation;

    private Set<String> dietaryRestrictions;
    private Set<String> medicalConditions;
    private String specialAccommodations;
    private String source;
}

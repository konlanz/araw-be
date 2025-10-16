package com.araw.araw.application.dto.application;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationRequest {

    private ApplicantInfoDto applicantInfo;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 2000, message = "Motivation statement must not exceed 2000 characters")
    private String motivationStatement;

    @Size(max = 2000, message = "Prior experience must not exceed 2000 characters")
    private String priorExperience;

    @Size(max = 1000, message = "Learning goals must not exceed 1000 characters")
    private String learningGoals;

    private Map<String, String> customAnswers;

    private Boolean guardianConsent;
    private String guardianName;

    @Email(message = "Invalid guardian email format")
    private String guardianEmail;

    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number")
    private String guardianPhone;

    private String emergencyContactName;

    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid emergency phone number")
    private String emergencyContactPhone;

    private String emergencyContactRelation;

    private Set<String> dietaryRestrictions;
    private Set<String> medicalConditions;
    private String specialAccommodations;
}


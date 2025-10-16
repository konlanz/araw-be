package com.araw.araw.application.dto.participant;

import com.araw.araw.domain.application.valueobject.EducationLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParticipantRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String preferredName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String gender;

    @Valid
    private ContactInfoDto contactInfo;

    private EducationLevel educationLevel;

    private String schoolName;

    private String gradeLevel;

    private Integer graduationYear;

    private Set<String> interests;

    private Set<String> skills;

    private Boolean consentForCommunication;

    private Boolean consentForPhotos;

    private Boolean consentForTestimonials;

    private String linkedinUrl;

    private String portfolioUrl;

    private String profilePictureUrl;

    @Size(max = 2000, message = "Alumni story must not exceed 2000 characters")
    private String alumniStory;

    @Size(max = 200, message = "Current status must not exceed 200 characters")
    private String currentStatus;

    private String currentInstitution;

    private String currentField;

    private Boolean alumni;

    private Integer totalEventsAttended;

    private Integer totalHoursParticipated;

    private Boolean featuredAlumni;
}

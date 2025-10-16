package com.araw.araw.application.dto.application;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantInfoDto {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    private String middleName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private String gender;
    private String ethnicity;

    @Pattern(regexp = "\\+?[1-9]\\d{1,14}", message = "Invalid phone number")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    private String stateProvince;

    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "School name is required")
    private String schoolName;

    @NotBlank(message = "Grade level is required")
    private String gradeLevel;

    @DecimalMin(value = "0.0", message = "GPA cannot be negative")
    @DecimalMax(value = "5.0", message = "GPA cannot exceed 5.0")
    private Double gpa;

    private String preferredLanguage;
}


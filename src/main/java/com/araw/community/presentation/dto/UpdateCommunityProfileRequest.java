package com.araw.community.presentation.dto;

import com.araw.community.domain.model.ProfileType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UpdateCommunityProfileRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotNull(message = "Profile type is required")
        ProfileType profileType,
        OffsetDateTime joinedAt,
        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must be at most 160 characters")
        String email,
        @Size(max = 40, message = "Phone number must be at most 40 characters")
        String phone,
        @Size(max = 160, message = "Headline must be at most 160 characters")
        String headline,
        String biography,
        @Size(max = 160, message = "Organization must be at most 160 characters")
        String organization,
        @Size(max = 160, message = "Location must be at most 160 characters")
        String location,
        Set<@Size(min = 2, max = 50) String> skills,
        Set<@Size(min = 2, max = 50) String> interests,
        UUID profileMediaId,
        Boolean featured,
        Boolean active,
        String slug
) {
}

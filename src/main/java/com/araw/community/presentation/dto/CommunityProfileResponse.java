package com.araw.community.presentation.dto;

import com.araw.community.domain.model.ProfileType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CommunityProfileResponse(
        UUID id,
        String slug,
        String firstName,
        String lastName,
        String fullName,
        ProfileType profileType,
        String email,
        String phone,
        String headline,
        String biography,
        String organization,
        String location,
        Set<String> skills,
        Set<String> interests,
        OffsetDateTime joinedAt,
        boolean active,
        boolean featured,
        UUID profileMediaId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

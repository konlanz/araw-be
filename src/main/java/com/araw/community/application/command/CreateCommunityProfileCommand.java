package com.araw.community.application.command;

import com.araw.community.domain.model.ProfileType;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CreateCommunityProfileCommand(
        String firstName,
        String lastName,
        ProfileType profileType,
        OffsetDateTime joinedAt,
        String email,
        String phone,
        String headline,
        String biography,
        String organization,
        String location,
        Set<String> skills,
        Set<String> interests,
        UUID profileMediaId,
        Boolean featured,
        String slug
) {
}

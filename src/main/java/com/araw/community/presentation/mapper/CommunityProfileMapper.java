package com.araw.community.presentation.mapper;

import com.araw.community.application.command.CreateCommunityProfileCommand;
import com.araw.community.application.command.UpdateCommunityProfileCommand;
import com.araw.community.domain.model.CommunityProfile;
import com.araw.community.presentation.dto.CommunityProfileResponse;
import com.araw.community.presentation.dto.CreateCommunityProfileRequest;
import com.araw.community.presentation.dto.UpdateCommunityProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommunityProfileMapper {

    @Mapping(target = "fullName", expression = "java(profile.getFullName())")
    CommunityProfileResponse toResponse(CommunityProfile profile);

    default CreateCommunityProfileCommand toCreateCommand(CreateCommunityProfileRequest request) {
        return new CreateCommunityProfileCommand(
                request.firstName(),
                request.lastName(),
                request.profileType(),
                request.joinedAt(),
                request.email(),
                request.phone(),
                request.headline(),
                request.biography(),
                request.organization(),
                request.location(),
                normalize(request.skills()),
                normalize(request.interests()),
                request.profileMediaId(),
                request.featured(),
                request.slug()
        );
    }

    default UpdateCommunityProfileCommand toUpdateCommand(UUID id, UpdateCommunityProfileRequest request) {
        return new UpdateCommunityProfileCommand(
                id,
                request.firstName(),
                request.lastName(),
                request.profileType(),
                request.joinedAt(),
                request.email(),
                request.phone(),
                request.headline(),
                request.biography(),
                request.organization(),
                request.location(),
                normalize(request.skills()),
                normalize(request.interests()),
                request.profileMediaId(),
                request.featured(),
                request.active(),
                request.slug()
        );
    }

    private Set<String> normalize(Set<String> values) {
        return values == null ? Set.of() : values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}

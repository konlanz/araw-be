package com.araw.community.presentation;

import com.araw.community.application.CommunityProfileApplicationService;
import com.araw.community.domain.model.ProfileType;
import com.araw.community.presentation.dto.CommunityProfileResponse;
import com.araw.community.presentation.dto.CreateCommunityProfileRequest;
import com.araw.community.presentation.dto.UpdateCommunityProfileRequest;
import com.araw.community.presentation.mapper.CommunityProfileMapper;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/community/profiles")
@RequiredArgsConstructor
public class CommunityProfileController {

    private final CommunityProfileApplicationService profileService;
    private final CommunityProfileMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityProfileResponse createProfile(@Valid @RequestBody CreateCommunityProfileRequest request) {
        var command = mapper.toCreateCommand(request);
        var profile = profileService.createProfile(command);
        return mapper.toResponse(profile);
    }

    @PutMapping("/{profileId}")
    public CommunityProfileResponse updateProfile(@PathVariable UUID profileId,
                                                  @Valid @RequestBody UpdateCommunityProfileRequest request) {
        var command = mapper.toUpdateCommand(profileId, request);
        var profile = profileService.updateProfile(command);
        return mapper.toResponse(profile);
    }

    @PatchMapping("/{profileId}/activate")
    public CommunityProfileResponse activateProfile(@PathVariable UUID profileId) {
        var profile = profileService.activateProfile(profileId);
        return mapper.toResponse(profile);
    }

    @PatchMapping("/{profileId}/deactivate")
    public CommunityProfileResponse deactivateProfile(@PathVariable UUID profileId) {
        var profile = profileService.deactivateProfile(profileId);
        return mapper.toResponse(profile);
    }

    @PatchMapping("/{profileId}/feature")
    public CommunityProfileResponse featureProfile(@PathVariable UUID profileId) {
        var profile = profileService.featureProfile(profileId);
        return mapper.toResponse(profile);
    }

    @PatchMapping("/{profileId}/unfeature")
    public CommunityProfileResponse unfeatureProfile(@PathVariable UUID profileId) {
        var profile = profileService.unfeatureProfile(profileId);
        return mapper.toResponse(profile);
    }

    @GetMapping
    public PagedResponse<CommunityProfileResponse> searchProfiles(
            @RequestParam(value = "type", required = false) ProfileType profileType,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "featured", required = false) Boolean featured,
            Pageable pageable) {
        Page<CommunityProfileResponse> page = profileService.search(profileType, active, featured, pageable)
                .map(mapper::toResponse);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/{profileId}")
    public CommunityProfileResponse getProfile(@PathVariable UUID profileId) {
        var profile = profileService.getById(profileId);
        return mapper.toResponse(profile);
    }

    @GetMapping("/slug/{slug}")
    public CommunityProfileResponse getProfileBySlug(@PathVariable String slug) {
        var profile = profileService.getBySlug(slug);
        return mapper.toResponse(profile);
    }
}

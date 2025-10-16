package com.araw.community.application;

import com.araw.community.application.command.CreateCommunityProfileCommand;
import com.araw.community.application.command.UpdateCommunityProfileCommand;
import com.araw.community.domain.model.CommunityProfile;
import com.araw.community.domain.model.ProfileType;
import com.araw.community.domain.repository.CommunityProfileRepository;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.text.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityProfileApplicationService {

    private final CommunityProfileRepository profileRepository;
    private final SlugGenerator slugGenerator;

    public CommunityProfile createProfile(CreateCommunityProfileCommand command) {
        ProfileType profileType = Objects.requireNonNull(command.profileType(), "Profile type is required");
        String slug = resolveUniqueSlug(command.slug(), command.firstName(), command.lastName(), null);

        CommunityProfile profile = CommunityProfile.create(
                slug,
                command.firstName(),
                command.lastName(),
                profileType,
                command.joinedAt(),
                command.email(),
                command.phone(),
                command.headline(),
                command.biography(),
                command.organization(),
                command.location(),
                command.skills(),
                command.interests(),
                command.profileMediaId(),
                command.featured() != null && command.featured()
        );

        return profileRepository.save(profile);
    }

    public CommunityProfile updateProfile(UpdateCommunityProfileCommand command) {
        CommunityProfile profile = getById(command.id());

        if (command.slug() != null && !command.slug().isBlank()) {
            String candidateSlug = resolveUniqueSlug(command.slug(), command.firstName(), command.lastName(), profile.getId());
            profile.setSlug(candidateSlug);
        }

        profile.updateProfile(
                command.firstName(),
                command.lastName(),
                command.profileType(),
                command.joinedAt(),
                command.email(),
                command.phone(),
                command.headline(),
                command.biography(),
                command.organization(),
                command.location(),
                command.skills(),
                command.interests(),
                command.profileMediaId(),
                command.featured(),
                command.active()
        );

        return profileRepository.save(profile);
    }

    public CommunityProfile activateProfile(UUID profileId) {
        CommunityProfile profile = getById(profileId);
        profile.activate();
        return profileRepository.save(profile);
    }

    public CommunityProfile deactivateProfile(UUID profileId) {
        CommunityProfile profile = getById(profileId);
        profile.deactivate();
        return profileRepository.save(profile);
    }

    public CommunityProfile featureProfile(UUID profileId) {
        CommunityProfile profile = getById(profileId);
        profile.markFeatured();
        return profileRepository.save(profile);
    }

    public CommunityProfile unfeatureProfile(UUID profileId) {
        CommunityProfile profile = getById(profileId);
        profile.unmarkFeatured();
        return profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public CommunityProfile getById(UUID id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new DomainNotFoundException("Community profile not found: " + id));
    }

    @Transactional(readOnly = true)
    public CommunityProfile getBySlug(String slug) {
        return profileRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainNotFoundException("Community profile not found for slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<CommunityProfile> search(ProfileType profileType,
                                         Boolean active,
                                         Boolean featured,
                                         Pageable pageable) {
        return profileRepository.search(profileType, active, featured, pageable);
    }

    private String resolveUniqueSlug(String requestedSlug,
                                     String firstName,
                                     String lastName,
                                     UUID currentProfileId) {
        String baseInput;
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            baseInput = requestedSlug;
        } else {
            baseInput = "%s-%s".formatted(
                    Objects.requireNonNullElse(firstName, ""),
                    Objects.requireNonNullElse(lastName, "")
            );
        }

        String baseSlug = slugGenerator.generateSlug(baseInput);

        String candidate = baseSlug;
        int attempt = 1;
        while (true) {
            var existing = profileRepository.findBySlug(candidate);
            if (existing.isEmpty()) {
                return candidate;
            }
            if (currentProfileId != null && existing.get().getId().equals(currentProfileId)) {
                return candidate;
            }
            candidate = "%s-%d".formatted(baseSlug, attempt++);
        }
    }
}

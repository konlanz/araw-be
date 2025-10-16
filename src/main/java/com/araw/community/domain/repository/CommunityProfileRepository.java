package com.araw.community.domain.repository;

import com.araw.community.domain.model.CommunityProfile;
import com.araw.community.domain.model.ProfileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CommunityProfileRepository {

    CommunityProfile save(CommunityProfile profile);

    Optional<CommunityProfile> findById(UUID id);

    Optional<CommunityProfile> findBySlug(String slug);

    Page<CommunityProfile> search(ProfileType type, Boolean active, Boolean featured, Pageable pageable);
}

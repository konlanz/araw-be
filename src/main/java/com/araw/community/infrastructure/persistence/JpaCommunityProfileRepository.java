package com.araw.community.infrastructure.persistence;

import com.araw.community.domain.model.CommunityProfile;
import com.araw.community.domain.model.ProfileType;
import com.araw.community.domain.repository.CommunityProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCommunityProfileRepository extends CommunityProfileRepository,
        JpaRepository<CommunityProfile, UUID>,
        JpaSpecificationExecutor<CommunityProfile> {

    @Override
    Optional<CommunityProfile> findBySlug(String slug);

    @Override
    default Page<CommunityProfile> search(ProfileType type, Boolean active, Boolean featured, Pageable pageable) {
        Specification<CommunityProfile> spec = Specification.where(null);
        if (type != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("profileType"), type));
        }
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        if (featured != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("featured"), featured));
        }
        return findAll(spec, pageable);
    }
}

package com.araw.media.infrastructure.persistence;

import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import com.araw.media.domain.repository.MediaAssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMediaAssetRepository extends MediaAssetRepository, JpaRepository<MediaAsset, UUID> {

    @Override
    Optional<MediaAsset> findByObjectKey(String objectKey);

    @Override
    Page<MediaAsset> findAllByCategory(MediaCategory category, Pageable pageable);

    @Override
    long countByCategory(MediaCategory category);
}

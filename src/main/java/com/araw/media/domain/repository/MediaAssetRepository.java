package com.araw.media.domain.repository;

import com.araw.media.domain.model.MediaAsset;
import com.araw.media.domain.model.MediaCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository {

    MediaAsset save(MediaAsset asset);

    Optional<MediaAsset> findById(UUID id);

    Optional<MediaAsset> findByObjectKey(String objectKey);

    Page<MediaAsset> findAllByCategory(MediaCategory category, Pageable pageable);

    void delete(MediaAsset asset);
}

package com.araw.media.presentation.mapper;

import com.araw.media.domain.model.MediaAsset;
import com.araw.media.presentation.dto.MediaAssetResponse;
import org.springframework.stereotype.Component;

@Component
public class MediaAssetMapper {

    public MediaAssetResponse toResponse(MediaAsset asset, String presignedUrl) {
        return new MediaAssetResponse(
                asset.getId(),
                asset.getBucket(),
                asset.getObjectKey(),
                asset.getFileName(),
                asset.getContentType(),
                asset.getFileSize(),
                asset.getCategory(),
                asset.getEtag(),
                asset.getDescription(),
                presignedUrl,
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}

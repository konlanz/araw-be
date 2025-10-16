package com.araw.media.presentation.dto;

import com.araw.media.domain.model.MediaCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MediaAssetResponse(
        UUID id,
        String bucket,
        String objectKey,
        String fileName,
        String contentType,
        long fileSize,
        MediaCategory category,
        String etag,
        String description,
        String presignedUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

package com.araw.content.presentation.dto;

import com.araw.content.domain.model.ArticleStatus;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record ArticleResponse(
        UUID id,
        String slug,
        String title,
        String excerpt,
        String body,
        String authorName,
        Set<String> tags,
        ArticleStatus status,
        OffsetDateTime publishedAt,
        UUID heroMediaId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

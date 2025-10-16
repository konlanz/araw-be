package com.araw.content.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ArticlePublishedEvent(
        UUID articleId,
        String title,
        String slug,
        String excerpt,
        String authorName,
        OffsetDateTime publishedAt
) {
}

package com.araw.content.application.command;

import com.araw.content.domain.model.ArticleStatus;

import java.util.Set;
import java.util.UUID;

public record UpdateArticleCommand(
        UUID id,
        String title,
        String slug,
        String excerpt,
        String body,
        String authorName,
        Set<String> tags,
        UUID heroMediaId,
        ArticleStatus status
) {
}

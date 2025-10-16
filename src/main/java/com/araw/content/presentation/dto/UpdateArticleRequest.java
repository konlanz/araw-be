package com.araw.content.presentation.dto;

import com.araw.content.domain.model.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateArticleRequest(
        @NotBlank(message = "Title is required")
        String title,
        String slug,
        @Size(max = 512, message = "Excerpt must be at most 512 characters")
        String excerpt,
        @NotBlank(message = "Body is required")
        String body,
        @Size(max = 120, message = "Author name must be at most 120 characters")
        String authorName,
        Set<@Size(min = 2, max = 50) String> tags,
        UUID heroMediaId,
        ArticleStatus status
) {
}

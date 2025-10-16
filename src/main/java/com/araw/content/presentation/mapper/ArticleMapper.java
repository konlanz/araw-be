package com.araw.content.presentation.mapper;

import com.araw.content.application.command.CreateArticleCommand;
import com.araw.content.application.command.UpdateArticleCommand;
import com.araw.content.domain.model.Article;
import com.araw.content.presentation.dto.ArticleResponse;
import com.araw.content.presentation.dto.CreateArticleRequest;
import com.araw.content.presentation.dto.UpdateArticleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleMapper {

    ArticleResponse toResponse(Article article);

    default CreateArticleCommand toCreateCommand(CreateArticleRequest request) {
        return new CreateArticleCommand(
                request.title(),
                request.slug(),
                request.excerpt(),
                request.body(),
                request.authorName(),
                normalizeTags(request.tags()),
                request.heroMediaId(),
                request.status()
        );
    }

    default UpdateArticleCommand toUpdateCommand(UUID id, UpdateArticleRequest request) {
        return new UpdateArticleCommand(
                id,
                request.title(),
                request.slug(),
                request.excerpt(),
                request.body(),
                request.authorName(),
                normalizeTags(request.tags()),
                request.heroMediaId(),
                request.status()
        );
    }

    private Set<String> normalizeTags(Set<String> tags) {
        return tags == null ? Set.of() : tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet());
    }
}

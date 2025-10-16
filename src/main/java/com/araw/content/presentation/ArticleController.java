package com.araw.content.presentation;

import com.araw.content.application.ArticleApplicationService;
import com.araw.content.domain.model.ArticleStatus;
import com.araw.content.presentation.dto.ArticleResponse;
import com.araw.content.presentation.dto.CreateArticleRequest;
import com.araw.content.presentation.dto.UpdateArticleRequest;
import com.araw.content.presentation.mapper.ArticleMapper;
import com.araw.shared.api.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleApplicationService articleService;
    private final ArticleMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @RequestBody CreateArticleRequest request) {
        var command = mapper.toCreateCommand(request);
        var article = articleService.createArticle(command);
        return mapper.toResponse(article);
    }

    @PutMapping("/{articleId}")
    public ArticleResponse updateArticle(@PathVariable UUID articleId,
                                         @Valid @RequestBody UpdateArticleRequest request) {
        var command = mapper.toUpdateCommand(articleId, request);
        var article = articleService.updateArticle(command);
        return mapper.toResponse(article);
    }

    @PatchMapping("/{articleId}/publish")
    public ArticleResponse publishArticle(@PathVariable UUID articleId) {
        var article = articleService.publishArticle(articleId);
        return mapper.toResponse(article);
    }

    @PatchMapping("/{articleId}/review")
    public ArticleResponse sendArticleToReview(@PathVariable UUID articleId) {
        var article = articleService.moveArticleToReview(articleId);
        return mapper.toResponse(article);
    }

    @PatchMapping("/{articleId}/archive")
    public ArticleResponse archiveArticle(@PathVariable UUID articleId) {
        var article = articleService.archiveArticle(articleId);
        return mapper.toResponse(article);
    }

    @GetMapping
    public PagedResponse<ArticleResponse> listArticles(@RequestParam(value = "status", required = false) ArticleStatus status,
                                                       Pageable pageable) {
        Page<ArticleResponse> page = articleService.listArticles(status, pageable)
                .map(mapper::toResponse);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/{articleId}")
    public ArticleResponse getArticle(@PathVariable UUID articleId) {
        var article = articleService.getById(articleId);
        return mapper.toResponse(article);
    }

    @GetMapping("/slug/{slug}")
    public ArticleResponse getArticleBySlug(@PathVariable String slug) {
        var article = articleService.getBySlug(slug);
        return mapper.toResponse(article);
    }
}

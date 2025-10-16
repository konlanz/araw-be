package com.araw.content.application;

import com.araw.content.application.command.CreateArticleCommand;
import com.araw.content.application.command.UpdateArticleCommand;
import com.araw.content.domain.event.ArticlePublishedEvent;
import com.araw.content.domain.model.Article;
import com.araw.content.domain.model.ArticleStatus;
import com.araw.content.domain.repository.ArticleRepository;
import com.araw.shared.exception.DomainNotFoundException;
import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.text.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleApplicationService {

    private final ArticleRepository articleRepository;
    private final SlugGenerator slugGenerator;
    private final ApplicationEventPublisher eventPublisher;

    public Article createArticle(CreateArticleCommand command) {
        ArticleStatus initialStatus = validateInitialStatus(command.status());
        String slug = resolveUniqueSlug(command.slug(), command.title(), null);
        Article article = Article.create(
                slug,
                command.title(),
                command.excerpt(),
                command.body(),
                command.authorName(),
                command.tags(),
                command.heroMediaId(),
                initialStatus
        );

        if (initialStatus == ArticleStatus.PUBLISHED) {
            article.publish();
        } else if (initialStatus == ArticleStatus.REVIEW) {
            article.moveToReview();
        }

        Article saved = articleRepository.save(article);
        if (saved.getStatus() == ArticleStatus.PUBLISHED) {
            publishArticlePublishedEvent(saved);
        }
        return saved;
    }

    public Article updateArticle(UpdateArticleCommand command) {
        Article article = getById(command.id());
        ArticleStatus previousStatus = article.getStatus();

        if (command.slug() != null && !command.slug().isBlank()) {
            if (!Objects.equals(article.getSlug(), command.slug().trim().toLowerCase())) {
                String slug = resolveUniqueSlug(command.slug(), command.title(), article.getId());
                article.setSlug(slug);
            }
        }

        article.updateContent(
                command.title(),
                command.excerpt(),
                command.body(),
                command.authorName(),
                command.tags(),
                command.heroMediaId()
        );

        ArticleStatus targetStatus = command.status();
        if (targetStatus != null) {
            applyStatusTransition(article, targetStatus);
        }

        Article saved = articleRepository.save(article);
        if (previousStatus != ArticleStatus.PUBLISHED && saved.getStatus() == ArticleStatus.PUBLISHED) {
            publishArticlePublishedEvent(saved);
        }
        return saved;
    }

    public Article publishArticle(UUID articleId) {
        Article article = getById(articleId);
        ArticleStatus previousStatus = article.getStatus();
        article.publish();
        Article saved = articleRepository.save(article);
        if (previousStatus != ArticleStatus.PUBLISHED && saved.getStatus() == ArticleStatus.PUBLISHED) {
            publishArticlePublishedEvent(saved);
        }
        return saved;
    }

    public Article moveArticleToReview(UUID articleId) {
        Article article = getById(articleId);
        article.moveToReview();
        return articleRepository.save(article);
    }

    public Article archiveArticle(UUID articleId) {
        Article article = getById(articleId);
        article.archive();
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Article getById(UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new DomainNotFoundException("Article not found: " + articleId));
    }

    @Transactional(readOnly = true)
    public Article getBySlug(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainNotFoundException("Article not found for slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<Article> listArticles(ArticleStatus status, Pageable pageable) {
        if (status != null) {
            return articleRepository.findAllByStatus(status, pageable);
        }
        return articleRepository.findAll(pageable);
    }

    private ArticleStatus validateInitialStatus(ArticleStatus status) {
        if (status == null) {
            return ArticleStatus.DRAFT;
        }
        if (status == ArticleStatus.ARCHIVED) {
            throw new DomainValidationException("Cannot create an article directly in ARCHIVED state");
        }
        return status;
    }

    private void applyStatusTransition(Article article, ArticleStatus targetStatus) {
        if (targetStatus == article.getStatus()) {
            return;
        }
        switch (targetStatus) {
            case DRAFT -> article.setStatus(ArticleStatus.DRAFT);
            case REVIEW -> article.moveToReview();
            case PUBLISHED -> article.publish();
            case ARCHIVED -> article.archive();
        }
    }

    private String resolveUniqueSlug(String requestedSlug, String title, UUID currentArticleId) {
        String base = requestedSlug != null && !requestedSlug.isBlank()
                ? slugGenerator.generateSlug(requestedSlug)
                : slugGenerator.generateSlug(title);

        String candidate = base;
        int attempt = 1;
        while (true) {
            var existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty()) {
                return candidate;
            }
            if (currentArticleId != null && existing.get().getId().equals(currentArticleId)) {
                return candidate;
            }
            candidate = "%s-%d".formatted(base, attempt++);
        }
    }

    private void publishArticlePublishedEvent(Article article) {
        OffsetDateTime publishedAt = article.getPublishedAt() != null
                ? article.getPublishedAt()
                : OffsetDateTime.now();
        eventPublisher.publishEvent(new ArticlePublishedEvent(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getExcerpt(),
                article.getAuthorName(),
                publishedAt
        ));
    }
}

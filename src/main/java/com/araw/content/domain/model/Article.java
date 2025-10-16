package com.araw.content.domain.model;

import com.araw.shared.exception.DomainValidationException;
import com.araw.shared.persistence.AuditableEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "articles")
public class Article extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "excerpt", length = 512)
    private String excerpt;

    @Lob
    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "author_name", length = 120)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "hero_media_id")
    private UUID heroMediaId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag", nullable = false, length = 50)
    private Set<String> tags = new HashSet<>();

    public static Article create(String slug,
                                 String title,
                                 String excerpt,
                                 String body,
                                 String authorName,
                                 Set<String> tags,
                                 UUID heroMediaId,
                                 ArticleStatus status) {
        Article article = new Article();
        article.setSlug(slug);
        article.setTitle(title);
        article.setExcerpt(excerpt);
        article.setBody(body);
        article.setAuthorName(authorName);
        article.setStatus(status != null ? status : ArticleStatus.DRAFT);
        article.setTags(tags);
        article.heroMediaId = heroMediaId;
        return article;
    }

    public void setSlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new DomainValidationException("Article slug must not be blank");
        }
        this.slug = slug.trim().toLowerCase();
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new DomainValidationException("Article title must not be blank");
        }
        this.title = title.trim();
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt != null ? excerpt.trim() : null;
    }

    public void setBody(String body) {
        if (body == null || body.isBlank()) {
            throw new DomainValidationException("Article body must not be blank");
        }
        this.body = body;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName != null ? authorName.trim() : null;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status != null ? status : ArticleStatus.DRAFT;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags != null ? new HashSet<>(tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(String::toLowerCase)
                .toList()) : new HashSet<>();
    }

    public void updateContent(String title,
                              String excerpt,
                              String body,
                              String authorName,
                              Set<String> tags,
                              UUID heroMediaId) {
        setTitle(title);
        setExcerpt(excerpt);
        setBody(body);
        setAuthorName(authorName);
        setTags(tags);
        this.heroMediaId = heroMediaId;
    }

    public void publish() {
        if (this.status == ArticleStatus.PUBLISHED) {
            return;
        }
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = OffsetDateTime.now();
    }

    public void moveToReview() {
        if (this.status == ArticleStatus.ARCHIVED) {
            throw new DomainValidationException("Archived articles cannot be moved to review");
        }
        this.status = ArticleStatus.REVIEW;
    }

    public void archive() {
        this.status = ArticleStatus.ARCHIVED;
    }

    public void attachHeroMedia(UUID mediaId) {
        this.heroMediaId = mediaId;
    }

    public void detachHeroMedia() {
        this.heroMediaId = null;
    }
}

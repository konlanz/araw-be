package com.araw.content.infrastructure.persistence;

import com.araw.content.domain.model.Article;
import com.araw.content.domain.model.ArticleStatus;
import com.araw.content.domain.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaArticleRepository extends ArticleRepository, JpaRepository<Article, UUID> {

    @Override
    Optional<Article> findBySlug(String slug);

    @Override
    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);
}

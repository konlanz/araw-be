package com.araw.content.domain.repository;

import com.araw.content.domain.model.Article;
import com.araw.content.domain.model.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository {

    Article save(Article article);

    Optional<Article> findById(UUID id);

    Optional<Article> findBySlug(String slug);

    Page<Article> findAll(Pageable pageable);

    Page<Article> findAllByStatus(ArticleStatus status, Pageable pageable);
}

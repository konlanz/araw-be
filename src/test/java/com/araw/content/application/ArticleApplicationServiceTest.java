package com.araw.content.application;

import com.araw.content.application.command.CreateArticleCommand;
import com.araw.content.domain.event.ArticlePublishedEvent;
import com.araw.content.domain.model.Article;
import com.araw.content.domain.model.ArticleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@RecordApplicationEvents
class ArticleApplicationServiceTest {

    @Autowired
    private ArticleApplicationService articleService;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    void createArticleGeneratesUniqueSlug() {
        Article first = articleService.createArticle(new CreateArticleCommand(
                "Future of STEM Volunteers",
                null,
                "Exploring the next chapter for volunteers.",
                "Body content",
                "ARAW Editorial Team",
                Set.of("STEM", "volunteers"),
                null,
                ArticleStatus.DRAFT
        ));

        Article second = articleService.createArticle(new CreateArticleCommand(
                "Future of STEM Volunteers",
                null,
                "Another perspective.",
                "Updated body",
                "ARAW Editorial Team",
                Set.of("STEM"),
                null,
                ArticleStatus.DRAFT
        ));

        assertThat(first.getSlug()).isEqualTo("future-of-stem-volunteers");
        assertThat(second.getSlug()).isEqualTo("future-of-stem-volunteers-1");
    }

    @Test
    void publishArticleEmitsDomainEvent() {
        Article draft = articleService.createArticle(new CreateArticleCommand(
                "Grassroots Innovation Impact",
                null,
                "Community led innovations.",
                "Body content",
                "Innovation Desk",
                Set.of("innovation"),
                null,
                ArticleStatus.DRAFT
        ));

        applicationEvents.clear();

        articleService.publishArticle(draft.getId());

        assertThat(applicationEvents.stream(ArticlePublishedEvent.class)).hasSize(1);
        ArticlePublishedEvent event = applicationEvents.stream(ArticlePublishedEvent.class).findFirst().orElseThrow();
        assertThat(event.slug()).isEqualTo("grassroots-innovation-impact");
        assertThat(event.publishedAt()).isNotNull();
    }
}

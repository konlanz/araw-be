package com.araw.notification.listener;

import com.araw.content.domain.event.ArticlePublishedEvent;
import com.araw.notification.config.PublicationNotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ArticlePublicationEmailListener {

    private final JavaMailSender mailSender;
    private final PublicationNotificationProperties properties;
    private final MailProperties mailProperties;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onArticlePublished(ArticlePublishedEvent event) {
        if (!properties.isEnabled()) {
            return;
        }
        List<String> recipients = properties.getRecipients();
        if (recipients.isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipients.toArray(String[]::new));
        String fromAddress = resolveFromAddress();
        if (fromAddress != null && !fromAddress.isBlank()) {
            message.setFrom(fromAddress);
        }
        message.setSubject("New ARAW story: " + event.title());
        message.setText(buildBody(event));
        mailSender.send(message);
    }

    private String resolveFromAddress() {
        if (properties.getFromAddress() != null && !properties.getFromAddress().isBlank()) {
            return properties.getFromAddress();
        }
        return mailProperties.getUsername();
    }

    private String buildBody(ArticlePublishedEvent event) {
        String articleUrl = "%s/articles/%s".formatted(
                properties.getSiteUrl().replaceAll("/$", ""),
                event.slug()
        );
        String publishedAt = event.publishedAt() != null
                ? event.publishedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : "Just now";

        return """
                Hello ARAW Team,

                A new story has just been published on the platform.

                Title: %s
                Author: %s
                Published at: %s

                Summary:
                %s

                Read the full story: %s

                Regards,
                ARAW Publishing Bot
                """.formatted(
                event.title(),
                event.authorName() != null ? event.authorName() : "Unknown",
                publishedAt,
                event.excerpt() != null ? event.excerpt() : "(no summary provided)",
                articleUrl
        );
    }
}

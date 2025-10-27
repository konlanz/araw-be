package com.araw.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = "app.notifications.email")
public class EmailTemplateProperties {

    /**
     * Base path (relative to the classpath) where email template files live.
     * Defaults to {@code email/templates}.
     */
    private String templateBasePath = "email/templates";

    /**
     * Base URL used when auto-generating application links for published events.
     * Example: {@code https://apply.ara-w.org/events}.
     */
    private String applicationBaseUrl = "https://apply.ara-w.org/events";

    /**
     * Optional base URL that can be embedded in feedback invitations.
     * Example: {@code https://apply.ara-w.org/events/{{applicationSlug}}/feedback}.
     */
    private String feedbackBaseUrl;

    public String getTemplateBasePath() {
        return templateBasePath;
    }

    public void setTemplateBasePath(String templateBasePath) {
        if (templateBasePath == null || templateBasePath.isBlank()) {
            return;
        }
        String normalized = templateBasePath.strip();
        this.templateBasePath = normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }

    public String getApplicationBaseUrl() {
        return applicationBaseUrl;
    }

    public void setApplicationBaseUrl(String applicationBaseUrl) {
        if (applicationBaseUrl == null || applicationBaseUrl.isBlank()) {
            return;
        }
        this.applicationBaseUrl = trimTrailingSlash(applicationBaseUrl);
    }

    public String getFeedbackBaseUrl() {
        return feedbackBaseUrl;
    }

    public void setFeedbackBaseUrl(String feedbackBaseUrl) {
        if (feedbackBaseUrl == null || feedbackBaseUrl.isBlank()) {
            this.feedbackBaseUrl = null;
            return;
        }
        this.feedbackBaseUrl = trimTrailingSlash(feedbackBaseUrl);
    }

    private String trimTrailingSlash(String value) {
        String normalized = value.strip();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}

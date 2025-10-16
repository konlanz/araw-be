package com.araw.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.notifications.publication")
public class PublicationNotificationProperties {

    private boolean enabled = true;
    private List<String> recipients = new ArrayList<>();
    private String siteUrl = "https://www.ara-w.org";
    private String fromAddress;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRecipients() {
        return List.copyOf(recipients);
    }

    public void setRecipients(List<String> recipients) {
        if (recipients == null) {
            this.recipients = new ArrayList<>();
            return;
        }
        List<String> normalized = recipients.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .toList();
        this.recipients = new ArrayList<>(normalized);
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
}

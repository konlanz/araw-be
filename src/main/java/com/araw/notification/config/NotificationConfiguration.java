package com.araw.notification.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PublicationNotificationProperties.class)
public class NotificationConfiguration {
}

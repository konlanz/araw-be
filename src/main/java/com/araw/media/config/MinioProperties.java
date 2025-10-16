package com.araw.media.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.media.minio")
public class MinioProperties {

    @NotBlank
    private String endpoint;

    @NotBlank
    private String bucket;

    @NotBlank
    private String accessKey;

    @NotBlank
    private String secretKey;

    private String region = "";

    private boolean secure = false;

    @Min(1)
    private int presignedExpiryMinutes = 60;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public int getPresignedExpiryMinutes() {
        return presignedExpiryMinutes;
    }

    public void setPresignedExpiryMinutes(int presignedExpiryMinutes) {
        this.presignedExpiryMinutes = presignedExpiryMinutes;
    }

    public Duration presignedExpiry() {
        return Duration.ofMinutes(presignedExpiryMinutes);
    }
}

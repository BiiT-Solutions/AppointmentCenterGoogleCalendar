package com.biit.appointment.google.client;

import com.google.api.client.auth.oauth2.Credential;

import java.time.LocalDateTime;
import java.util.UUID;

public class CredentialData {

    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private Long expirationTimeMilliseconds;
    private Long refreshTokenExpirationTimeMilliseconds;
    private LocalDateTime createdAt;

    public CredentialData() {
        super();
        createdAt = LocalDateTime.now();
    }

    public CredentialData(String accessToken, String refreshToken, Long expirationTimeMilliseconds, Long refreshTokenExpirationTimeMilliseconds, UUID userId) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
        this.refreshTokenExpirationTimeMilliseconds = refreshTokenExpirationTimeMilliseconds;
        this.userId = userId;
    }

    public CredentialData(Credential credential, UUID user) {
        this();
        setAccessToken(credential.getAccessToken());
        setRefreshToken(credential.getRefreshToken());
        setExpirationTimeMilliseconds(credential.getExpirationTimeMilliseconds());
        setUserId(user);
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpirationTimeMilliseconds() {
        return expirationTimeMilliseconds;
    }

    public void setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    }

    public Long getRefreshTokenExpirationTimeMilliseconds() {
        return refreshTokenExpirationTimeMilliseconds;
    }

    public void setRefreshTokenExpirationTimeMilliseconds(Long refreshTokenExpirationTimeMilliseconds) {
        this.refreshTokenExpirationTimeMilliseconds = refreshTokenExpirationTimeMilliseconds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CredentialData{"
                + "userId=" + userId
                + ", accessToken='" + accessToken + '\''
                + ", refreshToken='" + refreshToken + '\''
                + ", expirationTimeMilliseconds=" + expirationTimeMilliseconds
                + ", refreshTokenExpirationTimeMilliseconds=" + refreshTokenExpirationTimeMilliseconds
                + '}';
    }
}

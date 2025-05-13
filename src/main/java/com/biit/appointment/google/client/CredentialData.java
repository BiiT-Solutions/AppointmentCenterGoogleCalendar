package com.biit.appointment.google.client;

import com.google.api.client.auth.oauth2.Credential;

import java.util.UUID;

public class CredentialData {

    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private Long expirationTimeMilliseconds;

    public CredentialData() {
        super();
    }

    public CredentialData(String accessToken, String refreshToken, Long expirationTimeMilliseconds, UUID userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
        this.userId = userId;
    }

    public CredentialData(Credential credential, UUID user) {
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
}

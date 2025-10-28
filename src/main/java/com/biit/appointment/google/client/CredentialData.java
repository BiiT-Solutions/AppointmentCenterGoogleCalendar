package com.biit.appointment.google.client;

/*-
 * #%L
 * Google Calendar Client
 * %%
 * Copyright (C) 2025 BiiT Sourcing Solutions S.L.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.api.client.auth.oauth2.Credential;
import com.google.gson.annotations.JsonAdapter;

import java.time.LocalDateTime;

public class CredentialData {

    private String accessToken;
    private String refreshToken;
    private Long expirationTimeMilliseconds;
    private Long refreshTokenExpirationTimeMilliseconds;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime createdAt;

    public CredentialData() {
        super();
        createdAt = LocalDateTime.now();
    }

    public CredentialData(String accessToken, String refreshToken, Long expirationTimeMilliseconds, Long refreshTokenExpirationTimeMilliseconds) {
        this();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
        this.refreshTokenExpirationTimeMilliseconds = refreshTokenExpirationTimeMilliseconds;
    }

    public CredentialData(Credential credential) {
        this();
        setAccessToken(credential.getAccessToken());
        setRefreshToken(credential.getRefreshToken());
        setExpirationTimeMilliseconds(credential.getExpirationTimeMilliseconds());
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
                + ", accessToken='" + accessToken + '\''
                + ", refreshToken='" + refreshToken + '\''
                + ", expirationTimeMilliseconds=" + expirationTimeMilliseconds
                + ", refreshTokenExpirationTimeMilliseconds=" + refreshTokenExpirationTimeMilliseconds
                + '}';
    }
}

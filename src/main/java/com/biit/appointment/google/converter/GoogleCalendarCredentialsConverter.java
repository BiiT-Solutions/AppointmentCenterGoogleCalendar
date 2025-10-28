package com.biit.appointment.google.converter;

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

import com.biit.appointment.core.models.CalendarProviderDTO;
import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.google.client.CredentialData;
import com.biit.appointment.google.client.GoogleClientProvider;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Component
public class GoogleCalendarCredentialsConverter {

    private final GoogleClientProvider googleClientProvider;

    public GoogleCalendarCredentialsConverter(GoogleClientProvider googleClientProvider) {
        this.googleClientProvider = googleClientProvider;
    }


    public ExternalCalendarCredentialsDTO convertElement(UUID user, Credential credential) {
        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO();
        externalCalendarCredentialsDTO.setCalendarProvider(CalendarProviderDTO.GOOGLE);
        externalCalendarCredentialsDTO.setUserId(user);
        externalCalendarCredentialsDTO.setCredentialData(new CredentialData(credential));
        return externalCalendarCredentialsDTO;
    }


    public ExternalCalendarCredentialsDTO convertElement(UUID user, CredentialData credentialData) {
        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO();
        externalCalendarCredentialsDTO.setCalendarProvider(CalendarProviderDTO.GOOGLE);
        externalCalendarCredentialsDTO.setUserId(user);
        externalCalendarCredentialsDTO.setCredentialData(credentialData);
        return externalCalendarCredentialsDTO;
    }

    public Credential reverse(ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO) throws GeneralSecurityException, IOException {
        return googleClientProvider.getCredentials(externalCalendarCredentialsDTO.getCredentialData(CredentialData.class));
    }
}

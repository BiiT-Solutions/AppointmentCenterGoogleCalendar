package com.biit.appointment.google.converter;

import com.biit.appointment.core.models.CalendarProviderDTO;
import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.google.client.CredentialData;
import com.biit.appointment.google.client.GoogleClient;
import com.google.api.client.auth.oauth2.Credential;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@Component
public class ExternalCalendarCredentialsConverter {

    private final GoogleClient googleClient;

    public ExternalCalendarCredentialsConverter(GoogleClient googleClient) {
        this.googleClient = googleClient;
    }


    public ExternalCalendarCredentialsDTO convertElement(UUID user, Credential credential) {
        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO();
        externalCalendarCredentialsDTO.setProvider(CalendarProviderDTO.GOOGLE);
        externalCalendarCredentialsDTO.setUserId(user);
        externalCalendarCredentialsDTO.setCredentialData(new CredentialData(credential, user));
        return externalCalendarCredentialsDTO;
    }

    public Credential reverse(ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO) throws GeneralSecurityException, IOException {
        return googleClient.getCredentials(externalCalendarCredentialsDTO.getCredentialData(CredentialData.class));
    }
}

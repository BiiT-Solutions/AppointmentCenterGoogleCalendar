package com.biit.appointment.google.converter;

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
        externalCalendarCredentialsDTO.setCredentialData(new CredentialData(credential, user));
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

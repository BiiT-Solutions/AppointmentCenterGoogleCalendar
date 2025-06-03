package com.biit.appointment.google.client;

import com.biit.appointment.core.exceptions.ExternalCalendarActionException;
import com.biit.appointment.core.exceptions.ExternalCalendarNotFoundException;
import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.core.models.CalendarProviderDTO;
import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.core.services.IExternalProviderCalendarService;
import com.biit.appointment.google.converter.AppointmentEventConverter;
import com.biit.appointment.google.converter.GoogleCalendarCredentialsConverter;
import com.biit.appointment.google.logger.GoogleCalDAVLogger;
import com.biit.server.exceptions.UserNotFoundException;
import com.biit.server.security.IAuthenticatedUser;
import com.biit.server.security.IAuthenticatedUserProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Controller
public class GoogleCalendarService implements IExternalProviderCalendarService {

    //Refresh tokens expires after six months of not using them (https://developers.google.com/identity/protocols/oauth2#expiration).
    private static final int REFRESH_TOKEN_EXPIRATION_DAYS = 90;
    private static final int MILLIS = 1000;

    private final GoogleClientProvider googleClientProvider;
    private final AppointmentEventConverter eventConverter;
    private final GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter;
    private final IAuthenticatedUserProvider authenticatedUserProvider;

    public GoogleCalendarService(GoogleClientProvider googleClientProvider, AppointmentEventConverter eventConverter,
                                 GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter,
                                 IAuthenticatedUserProvider authenticatedUserProvider) {
        this.googleClientProvider = googleClientProvider;
        this.eventConverter = eventConverter;
        this.googleCalendarCredentialsConverter = googleCalendarCredentialsConverter;
        this.authenticatedUserProvider = authenticatedUserProvider;

        GoogleCalDAVLogger.info(this.getClass(), "### Google Calendar Controller initialized");
    }

    @Override
    public CalendarProviderDTO from() {
        return CalendarProviderDTO.GOOGLE;
    }


    @Override
    public List<AppointmentDTO> getEvents(LocalDateTime startingFrom, LocalDateTime endingTo, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            return eventConverter.convertAll(googleClientProvider.getEvents(startingFrom, endingTo,
                    googleCalendarCredentialsConverter.reverse(credentials)));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }


    @Override
    public List<AppointmentDTO> getEvents(int numberOfEvents, LocalDateTime startingFrom, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            return eventConverter.convertAll(googleClientProvider.getEvents(numberOfEvents, startingFrom,
                    googleCalendarCredentialsConverter.reverse(credentials)));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    @Override
    public AppointmentDTO getEvent(String externalReference, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            return eventConverter.convert(googleClientProvider.getEvent(externalReference,
                    googleCalendarCredentialsConverter.reverse(credentials)));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    /**
     * Returns the external reference of the created event.
     *
     * @param appointmentDTO
     * @return
     */
    @Override
    public String addEvent(AppointmentDTO appointmentDTO, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            return googleClientProvider.createCalendarEvent(eventConverter.reverse(appointmentDTO),
                    googleCalendarCredentialsConverter.reverse(credentials));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    @Override
    public void deleteEvent(AppointmentDTO appointmentDTO, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            googleClientProvider.deleteCalendarEvent(appointmentDTO.getExternalReference(),
                    googleCalendarCredentialsConverter.reverse(credentials));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }


    public ExternalCalendarCredentialsDTO exchangeCodeForToken(String username, String code, String state) {
        final IAuthenticatedUser authenticatedUser = authenticatedUserProvider.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(this.getClass(),
                        "No user with username '" + username + "' found!"));
        return exchangeCodeForToken(UUID.fromString(authenticatedUser.getUID()), code, state, username);
    }


    public ExternalCalendarCredentialsDTO exchangeCodeForToken(UUID userUUID, String code, String state, String createdBy) {
        GoogleCalDAVLogger.debug(this.getClass(), "Requesting token for code '{}' and state '{}'.", code, state);
        try {
            final GoogleTokenResponse googleTokenResponse = googleClientProvider.exchangeCodeForToken(code, state);
            final CredentialData credentialData = new CredentialData(googleTokenResponse.getAccessToken(), googleTokenResponse.getRefreshToken(),
                    googleTokenResponse.getExpiresInSeconds() * 1000, userUUID);
            final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO(
                    userUUID, CalendarProviderDTO.GOOGLE);
            GoogleCalDAVLogger.debug(this.getClass(), "Credentials obtained from code successfully. Value\n{}", credentialData);
            externalCalendarCredentialsDTO.setCredentialData(credentialData);
            externalCalendarCredentialsDTO.setExpiresAt(credentialData.getCreatedAt().plusSeconds(googleTokenResponse.getExpiresInSeconds()));
            externalCalendarCredentialsDTO.setForceRefreshAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS));
            externalCalendarCredentialsDTO.setCreatedBy(createdBy);

            GoogleCalDAVLogger.debug(this.getClass(), "Token for user '{}' generated. Expires at '{}'.", userUUID,
                    externalCalendarCredentialsDTO.getExpiresAt());
            return externalCalendarCredentialsDTO;
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    @Override
    public ExternalCalendarCredentialsDTO updateToken(ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO) {
        try {
            final ExternalCalendarCredentialsDTO refreshedCalendarCredentials =
                    new ExternalCalendarCredentialsDTO(externalCalendarCredentialsDTO.getUserId(), CalendarProviderDTO.GOOGLE);
            final CredentialData credentialData = googleClientProvider.refreshCredentials(externalCalendarCredentialsDTO
                    .getCredentialData(CredentialData.class));
            GoogleCalDAVLogger.debug(this.getClass(), "Credential data refreshed. Value\n{}", credentialData);
            refreshedCalendarCredentials.setCredentialData(credentialData);
            refreshedCalendarCredentials.setExpiresAt(Instant.ofEpochMilli(
                    credentialData.getExpirationTimeMilliseconds()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            refreshedCalendarCredentials.setForceRefreshAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS));
            return refreshedCalendarCredentials;
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }
}

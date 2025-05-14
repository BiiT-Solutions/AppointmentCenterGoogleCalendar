package com.biit.appointment.google.client;

import com.biit.appointment.core.controllers.IExternalCredentialsController;
import com.biit.appointment.core.exceptions.ExternalCalendarActionException;
import com.biit.appointment.core.exceptions.ExternalCalendarNotFoundException;
import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.core.models.CalendarProviderDTO;
import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.core.providers.IExternalCalendarProvider;
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
public class GoogleCalendarController implements IExternalCalendarProvider {

    private final GoogleClient googleClient;
    private final AppointmentEventConverter eventConverter;
    private final GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter;
    private final IAuthenticatedUserProvider authenticatedUserProvider;
    private final IExternalCredentialsController externalCredentialsController;

    public GoogleCalendarController(GoogleClient googleClient, AppointmentEventConverter eventConverter,
                                    GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter,
                                    IAuthenticatedUserProvider authenticatedUserProvider, IExternalCredentialsController externalCredentialsController) {
        this.googleClient = googleClient;
        this.eventConverter = eventConverter;
        this.googleCalendarCredentialsConverter = googleCalendarCredentialsConverter;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.externalCredentialsController = externalCredentialsController;

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
            return eventConverter.convertAll(googleClient.getEvents(startingFrom, endingTo,
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
            return eventConverter.convertAll(googleClient.getEvents(numberOfEvents, startingFrom,
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
            return eventConverter.convert(googleClient.getEvent(externalReference,
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
            return googleClient.createCalendarEvent(eventConverter.reverse(appointmentDTO),
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
            googleClient.deleteCalendarEvent(appointmentDTO.getExternalReference(),
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
            final GoogleTokenResponse googleTokenResponse = googleClient.exchangeCodeForToken(code, state);
            final CredentialData credentialData = new CredentialData(googleTokenResponse.getAccessToken(), googleTokenResponse.getRefreshToken(),
                    googleTokenResponse.getExpiresInSeconds(), userUUID);
            final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO(
                    userUUID, CalendarProviderDTO.GOOGLE);
            externalCalendarCredentialsDTO.setCredentialData(credentialData);
            externalCalendarCredentialsDTO.setExpiresAt(Instant.ofEpochMilli(
                    credentialData.getExpirationTimeMilliseconds()).atZone(ZoneId.systemDefault()).toLocalDateTime());
            GoogleCalDAVLogger.debug(this.getClass(), "Token for user '{}' generated. Expires at '{}'.", userUUID, externalCalendarCredentialsDTO.getExpiresAt());
            return externalCredentialsController.create(externalCalendarCredentialsDTO, createdBy);
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    public void deleteToken(String username) {
        final IAuthenticatedUser authenticatedUser = authenticatedUserProvider.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(this.getClass(),
                        "No user with username '" + username + "' found!"));
        deleteToken(UUID.fromString(authenticatedUser.getUID()));
    }

    public void deleteToken(UUID userUUID) {
        externalCredentialsController.delete(userUUID, CalendarProviderDTO.GOOGLE);
    }
}

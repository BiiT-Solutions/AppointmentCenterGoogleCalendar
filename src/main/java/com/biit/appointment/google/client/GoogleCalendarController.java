package com.biit.appointment.google.client;

import com.biit.appointment.core.exceptions.ExternalCalendarActionException;
import com.biit.appointment.core.exceptions.ExternalCalendarNotFoundException;
import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.core.models.CalendarProviderDTO;
import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.core.providers.IExternalCalendarProvider;
import com.biit.appointment.google.converter.AppointmentEventConverter;
import com.biit.appointment.google.converter.ExternalCalendarCredentialsConverter;
import com.biit.appointment.google.logger.GoogleCalDAVLogger;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class GoogleCalendarController implements IExternalCalendarProvider {

    private final GoogleClient googleClient;
    private final AppointmentEventConverter eventConverter;
    private final ExternalCalendarCredentialsConverter externalCalendarCredentialsConverter;

    public GoogleCalendarController(GoogleClient googleClient, AppointmentEventConverter eventConverter, ExternalCalendarCredentialsConverter externalCalendarCredentialsConverter) {
        this.googleClient = googleClient;
        this.eventConverter = eventConverter;
        this.externalCalendarCredentialsConverter = externalCalendarCredentialsConverter;
    }

    @Override
    public CalendarProviderDTO from() {
        return CalendarProviderDTO.GOOGLE;
    }


    @Override
    public List<AppointmentDTO> getEvents(LocalDateTime startingFrom, LocalDateTime endingTo, ExternalCalendarCredentialsDTO credentials)
            throws ExternalCalendarActionException, ExternalCalendarNotFoundException {
        try {
            return eventConverter.convertAll(googleClient.getEvents(startingFrom, endingTo, null));
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
                    externalCalendarCredentialsConverter.reverse(credentials)));
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
                    externalCalendarCredentialsConverter.reverse(credentials)));
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
                    externalCalendarCredentialsConverter.reverse(credentials));
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
                    externalCalendarCredentialsConverter.reverse(credentials));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }
}

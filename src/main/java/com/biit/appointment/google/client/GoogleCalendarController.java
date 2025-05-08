package com.biit.appointment.google.client;

import com.biit.appointment.core.providers.ExternalCalendar;
import com.biit.appointment.core.exceptions.ExternalCalendarActionException;
import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.google.converter.AppointmentEventConverter;
import com.biit.appointment.google.logger.GoogleCalDAVLogger;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class GoogleCalendarController implements ExternalCalendar {

    private final GoogleClient googleClient;
    private final AppointmentEventConverter eventConverter;

    public GoogleCalendarController(GoogleClient googleClient, AppointmentEventConverter eventConverter) {
        this.googleClient = googleClient;
        this.eventConverter = eventConverter;
    }

    @Override
    public List<AppointmentDTO> getEvents(int numberOfEvents, LocalDateTime startingFrom) {
        try {
            return eventConverter.convertAll(googleClient.getEvents(numberOfEvents, startingFrom));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    @Override
    public AppointmentDTO getEvent(String externalReference) {
        try {
            return eventConverter.convert(googleClient.getEvent(externalReference));
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
    public String addEvent(AppointmentDTO appointmentDTO) {
        try {
            return googleClient.createCalendarEvent(eventConverter.reverse(appointmentDTO));
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }

    @Override
    public void deleteEvent(AppointmentDTO appointmentDTO) {
        try {
            googleClient.deleteCalendarEvent(appointmentDTO.getExternalReference());
        } catch (IOException | GeneralSecurityException e) {
            GoogleCalDAVLogger.errorMessage(this.getClass(), e);
            throw new ExternalCalendarActionException(this.getClass(), e);
        }
    }
}

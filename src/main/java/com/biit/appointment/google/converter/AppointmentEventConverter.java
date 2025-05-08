package com.biit.appointment.google.converter;

import com.biit.appointment.core.models.AppointmentDTO;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
public class AppointmentEventConverter {

    public static final String DELETED_STATUS = "cancelled";

    protected AppointmentDTO convertElement(Event from) {
        if (from == null) {
            return null;
        }
        final AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setTitle(from.getSummary());
        appointmentDTO.setDescription(from.getDescription());

        //All day event.
        if (from.getStart().getDate() != null) {
            appointmentDTO.setStartTime(Instant.ofEpochMilli(from.getStart().getDate().getValue())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            appointmentDTO.setEndTime(Instant.ofEpochMilli(from.getEnd().getDate().getValue())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            appointmentDTO.setAllDay(true);
        }

        //Normal event
        if (from.getStart().getDateTime() != null) {
            appointmentDTO.setStartTime(Instant.ofEpochMilli(from.getStart().getDateTime().getValue())
                    .atZone(ZoneId.of(from.getStart().getTimeZone())).toLocalDateTime());
            appointmentDTO.setEndTime(Instant.ofEpochMilli(from.getEnd().getDateTime().getValue())
                    .atZone(ZoneId.of(from.getEnd().getTimeZone())).toLocalDateTime());
            appointmentDTO.setAllDay(false);
        }

        appointmentDTO.setExternalReference(from.getId());
        appointmentDTO.setDeleted(Objects.equals(from.getStatus(), DELETED_STATUS));
        return appointmentDTO;
    }

    public AppointmentDTO convert(Event from) {
        return (from != null ? this.convertElement(from) : null);
    }


    public Event reverse(AppointmentDTO from) {
        if (from == null) {
            return null;
        }

        final Event event = new Event();
        event.setId(from.getExternalReference());
        event.setSummary(from.getTitle());
        event.setDescription(from.getDescription());

        if (from.isAllDay()) {
            final DateTime startingTime = new DateTime(true, from.getStartTime().toLocalDate()
                    .atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond() * 1000, null);
            final EventDateTime startDateTime = new EventDateTime().setDate(startingTime);
            event.setStart(startDateTime);
            event.setEnd(startDateTime);
        } else {
            final DateTime startingTime = new DateTime(Date.from(from.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
            final EventDateTime startDateTime = new EventDateTime().setDateTime(startingTime).setTimeZone(ZoneId.systemDefault().toString());
            event.setStart(startDateTime);

            final DateTime endingTime = new DateTime(Date.from(from.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
            final EventDateTime endingDateTime = new EventDateTime().setDateTime(endingTime).setTimeZone(ZoneId.systemDefault().toString());
            event.setEnd(endingDateTime);
        }

        if (from.isDeleted()) {
            event.setStatus(DELETED_STATUS);
        }
        return event;
    }


    public List<AppointmentDTO> convertAll(Collection<Event> from) {
        if (from == null) {
            return new ArrayList<>();
        }
        return from.stream().map(this::convert).toList();
    }


    public List<Event> reverseAll(Collection<AppointmentDTO> to) {
        return (to == null ? new ArrayList<>() : to.stream().map(this::reverse).toList());
    }
}

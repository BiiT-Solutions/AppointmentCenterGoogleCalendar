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

import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.core.models.CalendarProviderDTO;
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
        appointmentDTO.setCalendarProvider(CalendarProviderDTO.GOOGLE);

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

package com.biit.appointment.google.client;

import com.biit.appointment.core.models.AppointmentDTO;
import com.biit.appointment.google.converter.AppointmentEventConverter;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Test(groups = {"googleClientTest"})
public class GoogleClientTest {

    private static final int NUMBER_OF_EVENTS = 10;

    private List<Event> events;

    @Test
    public void getCalendarEvents() throws GeneralSecurityException, IOException {
        final GoogleClient googleClient = new GoogleClient();
        events = googleClient.getEvents(NUMBER_OF_EVENTS, new DateTime(System.currentTimeMillis()));
        googleClient.logEvents(events);
        Assert.assertEquals(events.size(), NUMBER_OF_EVENTS);
    }


    @Test(dependsOnMethods = "getCalendarEvents")
    public void convertEvents() {
        AppointmentEventConverter appointmentEventConverter = new AppointmentEventConverter();
        final List<AppointmentDTO> appointmentDTOs = appointmentEventConverter.convertAll(events);
        Assert.assertEquals(appointmentDTOs.size(), NUMBER_OF_EVENTS);
        
    }
}

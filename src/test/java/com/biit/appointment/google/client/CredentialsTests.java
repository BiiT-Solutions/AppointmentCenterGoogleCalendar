package com.biit.appointment.google.client;

import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.google.converter.GoogleCalendarCredentialsConverter;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Test(groups = {"googleCredentialsTest"})
public class CredentialsTests extends AbstractTestNGSpringContextTests {

    private UUID userId = UUID.fromString("5566b76f-cc42-4304-b28e-36c6ef6ddc6a");

    private Credential credential;

    @Autowired
    private GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter;

    @BeforeClass
    public void checkBeans() {
        Assert.assertNotNull(googleCalendarCredentialsConverter);
    }

    @BeforeClass
    public void getCredentials() throws GeneralSecurityException, IOException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final GoogleClient googleClient = new GoogleClient();
        credential = googleClient.getCredentials(userId.toString(), netHttpTransport);
    }

    @Test
    public void testCredentials() throws IOException, GeneralSecurityException {
        final GoogleClient googleClient = new GoogleClient();
        List<Event> events = googleClient.getEvents(1, new DateTime(System.currentTimeMillis()), credential);
        googleClient.logEvents(events);
        Assert.assertEquals(events.size(), 1);
    }

    @Test
    public void convertCredentials() throws GeneralSecurityException, IOException {
        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = googleCalendarCredentialsConverter.convertElement(userId, credential);
        Credential storedCredentials = googleCalendarCredentialsConverter.reverse(externalCalendarCredentialsDTO);

        final GoogleClient googleClient = new GoogleClient();
        List<Event> events = googleClient.getEvents(1, new DateTime(System.currentTimeMillis()), storedCredentials);
        googleClient.logEvents(events);
        Assert.assertEquals(events.size(), 1);
    }

//    @Test
//    public void testGoogleTapCredentials() throws GeneralSecurityException, IOException {
//        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO();
//        externalCalendarCredentialsDTO.setUserCredentials();
//        Credential storedCredentials = googleCalendarCredentialsConverter.reverse(externalCalendarCredentialsDTO);
//
//        final GoogleClient googleClient = new GoogleClient();
//        List<Event> events = googleClient.getEvents(1, new DateTime(System.currentTimeMillis()), storedCredentials);
//        googleClient.logEvents(events);
//        Assert.assertEquals(events.size(), 1);
//    }

}

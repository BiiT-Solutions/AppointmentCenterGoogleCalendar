package com.biit.appointment.google.client;

import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.core.utils.ObjectMapperFactory;
import com.biit.appointment.google.converter.GoogleCalendarCredentialsConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@Test(groups = {"googleCredentialsTest"})
public class CredentialsTests extends AbstractTestNGSpringContextTests {

    private UUID userId = UUID.fromString("360c9d8d-713a-4d88-b99d-1ced3a335785");

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
        final GoogleClientProvider googleClientProvider = new GoogleClientProvider();
        credential = googleClientProvider.getCredentials(userId.toString(), netHttpTransport);
    }

    @Test
    public void testCredentials() throws IOException, GeneralSecurityException {
        final GoogleClientProvider googleClientProvider = new GoogleClientProvider();
        List<Event> events = googleClientProvider.getEvents(1, new DateTime(System.currentTimeMillis()), credential);
        googleClientProvider.logEvents(events);
        Assert.assertEquals(events.size(), 1);
    }

    @Test
    public void convertCredentials() throws GeneralSecurityException, IOException {
        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = googleCalendarCredentialsConverter.convertElement(userId, credential);
        Credential storedCredentials = googleCalendarCredentialsConverter.reverse(externalCalendarCredentialsDTO);

        final GoogleClientProvider googleClientProvider = new GoogleClientProvider();
        List<Event> events = googleClientProvider.getEvents(1, new DateTime(System.currentTimeMillis()), storedCredentials);
        googleClientProvider.logEvents(events);
        Assert.assertEquals(events.size(), 1);
    }

//    @Test
//    public void testGoogleTapCredentials() throws GeneralSecurityException, IOException {
//        final ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = new ExternalCalendarCredentialsDTO();
//        Credential storedCredentials = googleCalendarCredentialsConverter.reverse(externalCalendarCredentialsDTO);
//
//        final GoogleClient googleClient = new GoogleClient();
//        List<Event> events = googleClient.getEvents(1, new DateTime(System.currentTimeMillis()), storedCredentials);
//        googleClient.logEvents(events);
//        Assert.assertEquals(events.size(), 1);
//    }

    @Test
    public void serializeAndDeserialize() throws URISyntaxException, IOException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        final CredentialData credentialData = objectMapper.readValue(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource("googleCredentials.txt").toURI()))).trim(), CredentialData.class);
        ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO = googleCalendarCredentialsConverter.convertElement(userId, credentialData);

        final CredentialData credentialData2 = externalCalendarCredentialsDTO.getCredentialData(CredentialData.class);
        Assert.assertEquals(credentialData.getAccessToken(), credentialData2.getAccessToken());
        Assert.assertEquals(credentialData.getRefreshToken(), credentialData2.getRefreshToken());
        Assert.assertEquals(credentialData.getCreatedAt(), credentialData2.getCreatedAt());
        Assert.assertEquals(credentialData.getExpirationTimeMilliseconds(), credentialData2.getExpirationTimeMilliseconds());
    }

}

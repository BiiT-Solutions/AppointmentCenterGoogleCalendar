package com.biit.appointment.google.client;

import com.biit.appointment.core.models.ExternalCalendarCredentialsDTO;
import com.biit.appointment.core.utils.ObjectMapperFactory;
import com.biit.appointment.google.converter.GoogleCalendarCredentialsConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@Test(groups = {"eventTests"})
public class EventTests extends AbstractTestNGSpringContextTests {

    private UUID userId = UUID.fromString("360c9d8d-713a-4d88-b99d-1ced3a335785");

    @Autowired
    private GoogleCalendarCredentialsConverter googleCalendarCredentialsConverter;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    private ExternalCalendarCredentialsDTO externalCalendarCredentialsDTO;

    @Test
    public void refreshToken() throws URISyntaxException, IOException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        final CredentialData credentialData = objectMapper.readValue(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource("googleCredentials.txt").toURI()))).trim(), CredentialData.class);

        externalCalendarCredentialsDTO = googleCalendarService.updateToken(googleCalendarCredentialsConverter.convertElement(userId, credentialData));
        Assert.assertNotNull(externalCalendarCredentialsDTO);
    }

    @Test(dependsOnMethods = "refreshToken")
    public void getEvents() throws URISyntaxException, IOException {
        final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
        final CredentialData credentialData = objectMapper.readValue(new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                .getResource("googleCredentials.txt").toURI()))).trim(), CredentialData.class);

        googleCalendarService.getEvents(LocalDateTime.now(), LocalDateTime.now().plusDays(3), googleCalendarCredentialsConverter.convertElement(userId, credentialData));
    }
}

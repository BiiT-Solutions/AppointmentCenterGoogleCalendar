package com.biit.appointment.google.client;

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

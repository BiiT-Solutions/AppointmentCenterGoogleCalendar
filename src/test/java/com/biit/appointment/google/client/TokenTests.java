package com.biit.appointment.google.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.UUID;

@SpringBootTest
@Test(groups = {"tokenTests"})
public class TokenTests extends AbstractTestNGSpringContextTests {

    private UUID userId = UUID.fromString("5566b76f-cc42-4304-b28e-36c6ef6ddc6a");
    private String oauthCode = "4/0AUJR-x5UCDfzFqD9LO3nBA42RPqNH3GKgvShKsW9-1vBrxwcL8d_v3gm6YxFn-Fl3qn97A";

    @Value("${google.client.state:}")
    private String clientState;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    /** Not working on localhost server */
    @Test
    public void convertCredentials() {
        //The oauthCode must be generated from the frontend to work with this test.
        googleCalendarService.exchangeCodeForToken(userId, oauthCode, clientState, null);
    }
}

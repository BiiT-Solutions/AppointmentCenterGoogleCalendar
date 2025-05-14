package com.biit.appointment.google.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

@SpringBootTest
@Test(groups = {"tokenTests"})
public class TokenTests extends AbstractTestNGSpringContextTests {

    private UUID userId = UUID.fromString("5566b76f-cc42-4304-b28e-36c6ef6ddc6a");
    private String token = "4/0AUJR-x4yIAjVMeFggCn1tpsD6WHXXKXjSwqYg7ox23gJA4iRJhVNIw8E1o4vQQNEuTf3qw";

    @Value("${google.client.state:}")
    private String clientState;

    @Autowired
    private GoogleCalendarController googleCalendarController;

    @Test
    public void convertCredentials() {
        //The token must be generated from the correct app to work this test.
        googleCalendarController.exchangeCodeForToken(userId, token, clientState, null);
    }


}

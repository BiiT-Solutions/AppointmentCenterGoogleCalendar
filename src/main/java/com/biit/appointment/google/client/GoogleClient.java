package com.biit.appointment.google.client;

import com.biit.appointment.google.logger.GoogleCalDAVLogger;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Clock;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class GoogleClient {

    private static final int DEFAULT_RECEIVER_PORT = 8888;
    private static final String DEFAULT_USER_ID = "user";

    private static final String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    private static final String AUTH_PROVIDER_FIELD = "auth_provider_x509_cert_url";
    private static final String AUTH_PROVIDER_URI = "https://www.googleapis.com/oauth2/v1/certs";
    private static final String PROJECT_ID_FIELD = "project_id";

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Calendar API Access";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * CalendarScopes.CALENDAR_READONLY if only can read.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "client_secret.json";

    private static final String PRIMARY_CALENDAR_ID = "primary";

    @Value("${google.receiver.port:" + DEFAULT_RECEIVER_PORT + "}")
    private Integer receiverPort;

    @Value("${google.client.id:}")
    private String clientId;

    @Value("${google.client.secret:}")
    private String clientSecret;

    @Value("${google.client.state:}")
    private String clientState;

    @Value("${google.project.id:}")
    private String projectId;

    @Value("${google.redirect.urls:}")
    private List<String> redirectUrls;

    @Value("${server.domain:localhost}")
    private String serverDomain;

    private Calendar calendarService;


    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private GoogleClientSecrets getCredentialsFromResources() throws IOException {
        // Load client secrets.
        final InputStream in = GoogleClient.class.getResourceAsStream(File.separator + CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     */
    private GoogleClientSecrets getCredentialsFromProperties() {
        if (clientId != null && !clientId.isEmpty()) {
            final GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
            clientSecrets.setInstalled(new GoogleClientSecrets.Details().setClientId(clientId).setClientSecret(clientSecret)
                    .setAuthUri(AUTH_URI).setTokenUri(TOKEN_URI).set(PROJECT_ID_FIELD, projectId).set(AUTH_PROVIDER_FIELD, AUTH_PROVIDER_URI)
                    .setRedirectUris(redirectUrls));
            return clientSecrets;
        }
        return null;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public Credential getCredentials(final NetHttpTransport netHttpTransport) throws IOException {
        return getCredentials(DEFAULT_USER_ID, netHttpTransport);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public Credential getCredentials(String userId, final NetHttpTransport netHttpTransport) throws IOException {
        final GoogleClientSecrets clientSecrets;
        if (clientSecret != null) {
            clientSecrets = getCredentialsFromProperties();
        } else {
            clientSecrets = getCredentialsFromResources();
        }

        if (clientSecrets != null) {
            // Build flow and trigger user authorization request.
            final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    netHttpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
            final LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(receiverPort != null ? receiverPort : DEFAULT_RECEIVER_PORT).build();
            //returns an authorized Credential object.
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize(userId);
        }
        return null;
    }


    public Credential getCredentials(CredentialData credentialData) throws IOException, GeneralSecurityException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final Credential credential = (new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()))
                .setTransport(netHttpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setTokenServerEncodedUrl(TOKEN_URI)
                .setClientAuthentication(new ClientParametersAuthentication(credentialData.getUserId().toString(),
                        this.clientSecret))
                .setClock(Clock.SYSTEM).build();


        credential.setAccessToken(credentialData.getAccessToken());
        credential.setRefreshToken(credentialData.getRefreshToken());
        credential.setExpirationTimeMilliseconds(credentialData.getExpirationTimeMilliseconds());

        return credential;
    }

    public CredentialData refreshCredentials(CredentialData credentialData) throws IOException, GeneralSecurityException {
        return refreshCredentials(credentialData.getRefreshToken(), this.clientId, this.clientSecret, credentialData.getUserId());
    }


    public CredentialData refreshCredentials(String refreshToken, String clientId, String clientSecret, UUID userId)
            throws IOException, GeneralSecurityException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final TokenResponse tokenResponse = new GoogleRefreshTokenRequest(netHttpTransport, JSON_FACTORY,
                refreshToken, clientId, clientSecret).setScopes(SCOPES).setGrantType("refresh_token").execute();

        return new CredentialData(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresInSeconds(), userId);
    }


    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        if (calendarService != null) {
            return calendarService;
        }
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return getCalendarService(getCredentials(netHttpTransport));
    }


    private Calendar getCalendarService(Credential credentials) throws IOException, GeneralSecurityException {
        if (calendarService == null && credentials != null) {
            final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            calendarService = new Calendar.Builder(netHttpTransport, JSON_FACTORY, credentials)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return calendarService;
    }


    public void logEvents(List<Event> events) {
        if (GoogleCalDAVLogger.isDebugEnabled()) {
            if (events.isEmpty()) {
                GoogleCalDAVLogger.info(this.getClass(), "No upcoming events found.");
            } else {
                GoogleCalDAVLogger.info(this.getClass(), "Upcoming events:");
                for (Event event : events) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    GoogleCalDAVLogger.debug(this.getClass(), "{} ({})", event.getSummary(), start);
                }
            }
        }
    }

    public List<Event> getEvents(int numberOfEvents, LocalDateTime startingFrom, Credential credential) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, numberOfEvents,
                new DateTime(Date.from(startingFrom.atZone(ZoneId.systemDefault()).toInstant())),
                credential);
    }

    public List<Event> getEvents(int numberOfEvents, DateTime startingFrom, Credential credential)
            throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, numberOfEvents, startingFrom, credential);
    }


    public List<Event> getEvents(String calendarId, int numberOfEvents, DateTime startingFrom, Credential credential)
            throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService(credential);

        if (service == null) {
            GoogleCalDAVLogger.warning(this.getClass(), "Google Calendar service is not correctly configured!");
            return new ArrayList<>();
        }

        // List the next N events from the primary calendar.
        final Events events = service.events().list(calendarId)
                .setMaxResults(numberOfEvents)
                .setTimeMin(startingFrom)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    public List<Event> getEvents(LocalDateTime startingFrom, LocalDateTime untilTo, Credential credential) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, new DateTime(Date.from(startingFrom.atZone(ZoneId.systemDefault()).toInstant())),
                new DateTime(Date.from(untilTo.atZone(ZoneId.systemDefault()).toInstant())),
                credential);
    }

    public List<Event> getEvents(DateTime startingFrom, DateTime untilTo, Credential credential)
            throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, startingFrom, untilTo, credential);
    }

    public List<Event> getEvents(String calendarId, DateTime startingFrom, DateTime untilTo, Credential credential)
            throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService(credential);
        if (service == null) {
            GoogleCalDAVLogger.warning(this.getClass(), "Google Calendar service is not correctly configured!");
            return new ArrayList<>();
        }

        // List the next N events from the primary calendar.
        final Events events = service.events().list(calendarId)
                .setTimeMin(startingFrom)
                .setTimeMax(untilTo)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }


    public Event getEvent(String eventId, Credential credential) throws IOException, GeneralSecurityException {
        return getEvent(PRIMARY_CALENDAR_ID, eventId, credential);
    }


    public Event getEvent(String calendarId, String eventId, Credential credential) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService(credential);
        if (service == null) {
            GoogleCalDAVLogger.warning(this.getClass(), "Google Calendar service is not correctly configured!");
            return null;
        }

        // List the next N events from the primary calendar.
        return service.events().get(calendarId, eventId).execute();
    }


    public String createCalendarEvent(Event event, Credential credential) throws IOException, GeneralSecurityException {
        return createCalendarEvent(PRIMARY_CALENDAR_ID, event, credential);
    }

    /**
     * Creates a new event on the Google Calendar.
     *
     * @param calendarId the calendar.
     * @param event      Details of the event to be created.
     * @return The ID of the newly created event.
     * @throws GeneralSecurityException If security credentials cannot be established.
     * @throws IOException              If an error occurs while communicating with the Google Calendar API.
     */
    public String createCalendarEvent(String calendarId, Event event, Credential credential) throws GeneralSecurityException, IOException {
        final Calendar service = getCalendarService(credential);

        if (service == null) {
            GoogleCalDAVLogger.warning(this.getClass(), "Google Calendar service is not correctly configured!");
            return null;
        }

        if (GoogleCalDAVLogger.isDebugEnabled()) {
            GoogleCalDAVLogger.debug(this.getClass(), "Creating event:\n{}", JSON_FACTORY.toPrettyString(event));
        }
        event = service.events().insert(calendarId, event).setSendNotifications(true).setConferenceDataVersion(1)
                .execute();
        GoogleCalDAVLogger.info(this.getClass(), "Event created: {}", event.getHtmlLink());
        return event.getId();
    }

    public void deleteCalendarEvent(String eventId, Credential credential) throws GeneralSecurityException, IOException {
        deleteCalendarEvent(PRIMARY_CALENDAR_ID, eventId, credential);
    }


    /**
     * When deleting an event, this event is still accessible by the API for a long time.
     *
     * @param calendarId
     * @param eventId
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void deleteCalendarEvent(String calendarId, String eventId, Credential credential) throws GeneralSecurityException, IOException {
        final Calendar service = getCalendarService(credential);
        if (service == null) {
            GoogleCalDAVLogger.warning(this.getClass(), "Google Calendar service is not correctly configured!");
            return;
        }

        final Event event = service.events().get(calendarId, eventId).execute();

        if (event != null) {
            GoogleCalDAVLogger.debug(this.getClass(), "Event deleted: {}", event.getHtmlLink());
            service.events().delete(calendarId, eventId).setSendUpdates("none").execute();
        } else {
            GoogleCalDAVLogger.debug(this.getClass(), "No event found with id '{}' on calendar '{}'.", eventId, calendarId);
        }
    }


    public GoogleTokenResponse exchangeCodeForToken(String code, String state) throws IOException, GeneralSecurityException {
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        if (!Objects.equals(state, clientState)) {
//            GoogleCalDAVLogger.severe(this.getClass(), "State '{}' does not match  with server '{}'.", state, clientState);
//            throw new AccessDeniedException("State value is incorrect!");
//        }
        final String redirectUri = "https://" + serverDomain;
        final GoogleAuthorizationCodeTokenRequest authorizationCodeTokenRequest = new GoogleAuthorizationCodeTokenRequest(
                netHttpTransport,
                JSON_FACTORY,
                TOKEN_URI,
                clientId,
                clientSecret,
                code,
                redirectUri
        );
        try {
            GoogleCalDAVLogger.debug(this.getClass(), "Token obtained successfully for code '{}' from google!", code);
            return authorizationCodeTokenRequest.execute();
        } catch (Exception e) {
            GoogleCalDAVLogger.debug(this.getClass(), "Failed to connect to google API with:\n"
                    + "projectId: '{}'\n"
                    + "clientId: '{}'\n"
                    + "clientSecret: '{}'\n"
                    + "code: '{}'\n"
                    + "state: '{}'\n"
                    + "redirectUri: '{}'", projectId, clientId, clientSecret, redirectUrls, code, state, redirectUri);
            throw e;
        }
    }

}

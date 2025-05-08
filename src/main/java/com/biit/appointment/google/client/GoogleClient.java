package com.biit.appointment.google.client;

import com.biit.appointment.google.logger.GoogleCalDAVLogger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
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
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class GoogleClient {

    private static final int DEFAULT_RECEIVER_PORT = 8888;

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

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.project.id}")
    private String projectId;

    @Value("#{${google.redirect.urls:'http://localhost'}}")
    private List<String> redirectUrls;

    private Calendar calendarService;


    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private GoogleClientSecrets getCredentialsFromResources() throws IOException {
        // Load client secrets.
        InputStream in = GoogleClient.class.getResourceAsStream(File.separator + CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return An authorized Credential object.
     */
    private GoogleClientSecrets getCredentialsFromProperties() {

        final GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(new GoogleClientSecrets.Details().setClientId(clientId).setClientSecret(clientSecret)
                .setAuthUri(AUTH_URI).setTokenUri(TOKEN_URI).set(PROJECT_ID_FIELD, projectId).set(AUTH_PROVIDER_FIELD, AUTH_PROVIDER_URI)
                .setRedirectUris(redirectUrls));
        return clientSecrets;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport netHttpTransport) throws IOException {
        final GoogleClientSecrets clientSecrets;
        if (clientSecret != null) {
            clientSecrets = getCredentialsFromProperties();
        } else {
            clientSecrets = getCredentialsFromResources();
        }

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                netHttpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(receiverPort != null ? receiverPort : DEFAULT_RECEIVER_PORT).build();
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        if (calendarService == null) {
            final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
            calendarService = new Calendar.Builder(netHttpTransport, JSON_FACTORY, getCredentials(netHttpTransport))
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

    public List<Event> getEvents(int numberOfEvents, LocalDateTime startingFrom) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, numberOfEvents,
                new DateTime(Date.from(startingFrom.atZone(ZoneId.systemDefault()).toInstant())));
    }

    public List<Event> getEvents(int numberOfEvents, DateTime startingFrom) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, numberOfEvents, startingFrom);
    }


    public List<Event> getEvents(String calendarId, int numberOfEvents, DateTime startingFrom) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService();

        // List the next N events from the primary calendar.
        Events events = service.events().list(calendarId)
                .setMaxResults(numberOfEvents)
                .setTimeMin(startingFrom)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

    public List<Event> getEvents(LocalDateTime startingFrom, LocalDateTime untilTo) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, new DateTime(Date.from(startingFrom.atZone(ZoneId.systemDefault()).toInstant())),
                new DateTime(Date.from(untilTo.atZone(ZoneId.systemDefault()).toInstant())));
    }

    public List<Event> getEvents(DateTime startingFrom, DateTime untilTo) throws IOException, GeneralSecurityException {
        return getEvents(PRIMARY_CALENDAR_ID, startingFrom, untilTo);
    }

    public List<Event> getEvents(String calendarId, DateTime startingFrom, DateTime untilTo) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService();

        // List the next N events from the primary calendar.
        Events events = service.events().list(calendarId)
                .setTimeMin(startingFrom)
                .setTimeMax(untilTo)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }


    public Event getEvent(String eventId) throws IOException, GeneralSecurityException {
        return getEvent(PRIMARY_CALENDAR_ID, eventId);
    }


    public Event getEvent(String calendarId, String eventId) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final Calendar service = getCalendarService();

        // List the next N events from the primary calendar.
        return service.events().get(calendarId, eventId).execute();
    }


    public String createCalendarEvent(Event event) throws IOException, GeneralSecurityException {
        return createCalendarEvent(PRIMARY_CALENDAR_ID, event);
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
    public String createCalendarEvent(String calendarId, Event event) throws GeneralSecurityException, IOException {
        final Calendar service = getCalendarService();

        if (GoogleCalDAVLogger.isDebugEnabled()) {
            GoogleCalDAVLogger.debug(this.getClass(), "Creating event:\n{}", JSON_FACTORY.toPrettyString(event));
        }
        event = service.events().insert(calendarId, event).setSendNotifications(true).setConferenceDataVersion(1)
                .execute();
        GoogleCalDAVLogger.info(this.getClass(), "Event created: {}", event.getHtmlLink());
        return event.getId();
    }

    public void deleteCalendarEvent(String eventId) throws GeneralSecurityException, IOException {
        deleteCalendarEvent(PRIMARY_CALENDAR_ID, eventId);
    }


    /**
     * When deleting an event, this event is still accessible by the API for a long time.
     *
     * @param calendarId
     * @param eventId
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void deleteCalendarEvent(String calendarId, String eventId) throws GeneralSecurityException, IOException {
        final Calendar service = getCalendarService();
        Event event = service.events().get(calendarId, eventId).execute();

        if (event != null) {
            GoogleCalDAVLogger.debug(this.getClass(), "Event deleted: {}", event.getHtmlLink());
            service.events().delete(calendarId, eventId).setSendUpdates("none").execute();
        } else {
            GoogleCalDAVLogger.debug(this.getClass(), "No event found with id '{}' on calendar '{}'.", eventId, calendarId);
        }
    }

}

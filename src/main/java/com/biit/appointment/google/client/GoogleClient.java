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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Component
public class GoogleClient {

    private static final int DEFAULT_RECEIVER_PORT = 88888;

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
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    private static final String PRIMARY_CALENDAR_ID = "primary";

    @Value("${google.receiver.port:" + DEFAULT_RECEIVER_PORT + "}")
    private Integer receiverPort;


    /**
     * Creates an authorized Credential object.
     *
     * @param netHttpTransport The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport netHttpTransport)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleClient.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

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
        final NetHttpTransport netHttpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(netHttpTransport, JSON_FACTORY, getCredentials(netHttpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    public void logEvents(List<Event> events) {
        if (events.isEmpty()) {
            GoogleCalDAVLogger.info(this.getClass(), "No upcoming events found.");
        } else {
            GoogleCalDAVLogger.info(this.getClass(), "Upcoming events:");
            for (Event event : events) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                GoogleCalDAVLogger.info(this.getClass(), "{} ({})", event.getSummary(), start);
            }
        }
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

        event = service.events().insert(calendarId, event).setSendNotifications(true).setConferenceDataVersion(1)
                .execute();
        GoogleCalDAVLogger.info(this.getClass(), "Event created: {}", event.getHtmlLink());
        return event.getId();
    }


    public void deleteCalendarEvent(String calendarId, String eventId) throws GeneralSecurityException, IOException {
        final Calendar service = getCalendarService();
        Event event = service.events().get(calendarId, eventId).execute();

        if (event != null) {
            GoogleCalDAVLogger.debug(this.getClass(), "Event deleted: {}", event.getHtmlLink());
            service.events().delete(calendarId, eventId).setSendNotifications(true).execute();
        } else {
            GoogleCalDAVLogger.debug(this.getClass(), "No event found with id '{}' on calendar '{}'.", eventId, calendarId);
        }
    }

}

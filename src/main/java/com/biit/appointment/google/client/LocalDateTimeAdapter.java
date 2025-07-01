package com.biit.appointment.google.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final int SECONDS_MAX_DECIMALS = 9;

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, SECONDS_MAX_DECIMALS, true)
            .toFormatter();

    @Override
    public JsonElement serialize(LocalDateTime localDate,
                                 Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(localDate.format(FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement jsonElement,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return LocalDateTime.parse(jsonElement.getAsJsonPrimitive().getAsString(), FORMATTER);
    }

}

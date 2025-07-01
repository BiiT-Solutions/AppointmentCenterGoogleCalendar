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

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final int DIGITS = 3;

    private static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatterBuilder().appendInstant(DIGITS).toFormatter();

    @Override
    public JsonElement serialize(LocalDateTime localDate,
                                 Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(localDate.format(ISO_INSTANT));
    }

    @Override
    public LocalDateTime deserialize(JsonElement jsonElement,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return LocalDateTime.parse(jsonElement.getAsJsonPrimitive().getAsString(), ISO_INSTANT);
    }

}

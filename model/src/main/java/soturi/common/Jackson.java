package soturi.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.androidrecord.AndroidRecordModule;

public class Jackson {
    private Jackson() {
        throw new UnsupportedOperationException();
    }

    public static ObjectMapper mapper = configure(new ObjectMapper());

    public static ObjectMapper configure(ObjectMapper mapper) {
        return mapper
            .registerModule(new JavaTimeModule())
            .registerModule(new AndroidRecordModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true);
    }
}

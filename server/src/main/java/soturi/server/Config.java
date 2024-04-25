package soturi.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Component
public final class Config {
    private final ObjectMapper objectMapper;
    private final File cfgFile = new File("config.json");
    public ConfigValues v = new ConfigValues();

    public Config(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        load();
    }

    public static class ConfigValues {
        public int giveFreeXpDelayInSeconds = 100;
        public long giveFreeXpAmount = 10000;
        public int spawnEnemyDelayInSeconds = 5;
        public int fightingMaxDistInMeters = 50;

        public List<String> geoNamesCountryCodes = List.of("PL");
        public String geoNamesDownloadDir = "GeoNames";

        public int geoMaxSplit = 5;
        public double geoMinLatitude = 49.97;
        public double geoMaxLatitude = 50.11;
        public double geoMinLongitude = 19.74;
        public double geoMaxLongitude = 20.09;
    }

    @SneakyThrows
    public void setValue(String key, String value) {
        Field field = ConfigValues.class.getField(key);
        Object mapped = objectMapper.readValue(value, field.getType());
        field.set(v, mapped);
        dump();
    }

    private void dump() {
        try {
            objectMapper.writeValue(cfgFile, v);
        }
        catch (IOException ignored) {
        }
    }
    private void load() {
        try {
            v = objectMapper.readValue(cfgFile, ConfigValues.class);
        }
        catch (IOException ignored) {
        }
    }

}

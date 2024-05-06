package soturi.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import soturi.content.GeoRegistry;
import soturi.model.RectangularArea;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Component
public final class Config {
    private final File configFile = new File("config.json");
    private final ObjectMapper objectMapper;
    public ConfigValues v = new ConfigValues();

    public Config(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        load();
        dump();
    }

    public static class ConfigValues {
        public int giveFreeXpDelayInSeconds = 100;
        public long giveFreeXpAmount = 100;
        public int spawnEnemyDelayInSeconds = 5;
        public int healDelayInSeconds = 1;
        public double healFraction = 0.10;
        public int fightingMaxDistInMeters = 60;

        public List<String> geoNamesCountryCodes = List.of("PL");
        public String geoNamesDownloadDir = "GeoNames";

        public int geoMaxSplit = 5;
        public double geoMinLatitude = 49.97;
        public double geoMaxLatitude = 50.11;
        public double geoMinLongitude = 19.74;
        public double geoMaxLongitude = 20.09;

        public void setGeoFromArea(RectangularArea area) {
            geoMinLatitude = area.lowerLatitude();
            geoMaxLatitude = area.upperLatitude();
            geoMinLongitude = area.lowerLongitude();
            geoMaxLongitude = area.upperLongitude();
        }
    }

    @SneakyThrows
    public void setValue(String key, String value) {
        if (key.equals("geo"))
            v.setGeoFromArea(GeoRegistry.KNOWN_AREAS.get(value));
        else {
            Field field = ConfigValues.class.getField(key);
            Object mapped = objectMapper.readValue(value, field.getType());
            field.set(v, mapped);
        }
        dump();
    }

    private void dump() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, v);
        }
        catch (IOException ignored) {
        }
    }
    private void load() {
        try {
            v = objectMapper.readValue(configFile, ConfigValues.class);
        }
        catch (IOException ignored) {
        }
    }

}

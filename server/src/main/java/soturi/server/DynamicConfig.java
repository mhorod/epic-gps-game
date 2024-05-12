package soturi.server;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import soturi.common.GeoProvider;
import soturi.common.Registry;
import soturi.model.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class DynamicConfig {
    private final File configFile = new File("config.json");
    private final ObjectMapper objectMapper;
    private final GeoProvider geoProvider;
    public Registry registry;

    public DynamicConfig(ObjectMapper objectMapper, GeoProvider geoProvider) {
        this.objectMapper = objectMapper;
        this.geoProvider = geoProvider;
        setConfigWithoutReloading(getDefaultConfig());
        tryToLoad();
        tryToDump();
    }

    public boolean tryToDump() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, registry.getConfig());
            return true;
        }
        catch (JacksonException exception) {
            throw new RuntimeException(exception);
        }
        catch (Exception ignored) {
            return false;
        }
    }
    public boolean tryToLoad() {
        try {
            setConfigWithoutReloading(objectMapper.readValue(configFile, Config.class));
            return true;
        }
        catch (JacksonException exception) {
            throw new RuntimeException(exception);
        }
        catch (Exception ignored) {
            return false;
        }
    }

    public void setConfigWithoutReloading(Config config) {
        try {
            String serial = objectMapper.writeValueAsString(config);
            Config deserial = objectMapper.readValue(serial, Config.class);
            if (!config.equals(deserial)) {
                log.error("config: {}", config);
                log.error("serial-deserial: {}", deserial);
                throw new RuntimeException("jackson does something strange");
            }
        } catch (Exception e) {
            throw e instanceof RuntimeException re ? re : new RuntimeException(e);
        }

        registry = new Registry(config, geoProvider);
    }

    public Config getDefaultConfig() {
        String defaultConfigFile = "default-config.json";
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(defaultConfigFile);
            if (is == null)
                throw new RuntimeException("resource " + defaultConfigFile + " is missing");
            return objectMapper.readValue(is, Config.class);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public Registry getRegistry() {
        return registry;
    }
}

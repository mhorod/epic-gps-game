package soturi.server.geo;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import soturi.server.DynamicConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
public class GeoNamesCityProvider implements CityProvider {
    private final String geoNamesDownloadDir = "static";
    private final DynamicConfig dynamicConfig;
    private final List<City> cities = new ArrayList<>();
    private List<String> lastCodes;

    @SneakyThrows(IOException.class)
    public GeoNamesCityProvider(DynamicConfig dynamicConfig) {
        this.dynamicConfig = dynamicConfig;
        Files.createDirectories(Paths.get(geoNamesDownloadDir));
    }

    private Path pathForCode(String code) {
        return Paths.get(geoNamesDownloadDir, code + "_GeoNames.txt");
    }

    @SneakyThrows(IOException.class)
    private void processCode(String code) {
        log.info("Processing code {}", code);
        if (!Files.exists(pathForCode(code)))
            downloadCode(code);
        Files
            .readAllLines(pathForCode(code))
            .stream()
            .map(GeoNamesEntry::parse)
            .map(GeoNamesEntry::toCity)
            .flatMap(Optional::stream)
            .forEach(cities::add);
    }

    @SneakyThrows(IOException.class)
    private ZipEntry findZipEntry(String name, ZipInputStream stream) {
        ZipEntry entry = stream.getNextEntry();
        while (entry != null && !entry.getName().equals(name))
            entry = stream.getNextEntry();
        if (entry == null)
            throw new RuntimeException("entry not found");
        return entry;
    }

    @SneakyThrows({URISyntaxException.class, IOException.class})
    private void downloadCode(String code) {
        log.info("Downloading data for code {}", code);
        URL url = new URI("https://download.geonames.org/export/dump/%s.zip".formatted(code)).toURL();
        URLConnection connection = url.openConnection();
        try (ZipInputStream zipStream = new ZipInputStream(connection.getInputStream())) {
            findZipEntry(code + ".txt", zipStream);
            Files.copy(zipStream, pathForCode(code));
        }
    }

    @Override
    public List<City> getCities() {
        List<String> newCodes = dynamicConfig.getRegistry().getCountryCodes();
        if (!newCodes.equals(lastCodes)) {
            cities.clear();
            lastCodes = newCodes;
            lastCodes.forEach(this::processCode);
        }
        return Collections.unmodifiableList(cities);
    }
}

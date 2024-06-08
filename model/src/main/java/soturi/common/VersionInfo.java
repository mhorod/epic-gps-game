package soturi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipFile;

public class VersionInfo {
    private VersionInfo() {
        throw new UnsupportedOperationException();
    }

    private static String readLineFromStream(InputStream stream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            return Objects.requireNonNull(reader.readLine());
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static String readLineFromResource(String resource) {
        try (InputStream stream = VersionInfo.class.getClassLoader().getResourceAsStream(resource)) {
            return readLineFromStream(stream);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static final String compilationTimeFile = "generated/compilation-time";
    private static final String commitIdFile = "generated/commit-id";

    public static final LocalDateTime compilationTime =
        LocalDateTime.parse(readLineFromResource(compilationTimeFile));
    public static final String commitId =
        readLineFromResource(commitIdFile);

    public static Optional<LocalDateTime> appCompilationTime() {
        try (ZipFile zip = new ZipFile("static/app.apk")) {
            InputStream stream = zip.getInputStream(zip.getEntry(compilationTimeFile));
            return Optional.of(LocalDateTime.parse(readLineFromStream(stream)));
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }
}

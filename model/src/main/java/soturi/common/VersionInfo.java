package soturi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Objects;

public class VersionInfo {
    private VersionInfo() {
        throw new UnsupportedOperationException();
    }

    private static String readLineFrom(String file) {
        try (InputStream is = VersionInfo.class.getClassLoader().getResourceAsStream(file)) {
            Objects.requireNonNull(is);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return Objects.requireNonNull(reader.readLine());
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static final LocalDateTime compilationTime =
        LocalDateTime.parse(readLineFrom("generated/compilation-time"));

    public static final String commitId =
        readLineFrom("generated/commit-id");
}

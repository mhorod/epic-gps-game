package soturi.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class CompilationInfo {
    private CompilationInfo() {
        throw new UnsupportedOperationException();
    }

    private static Optional<String> readLineFrom(String file) {
        try {
            InputStream is = CompilationInfo.class.getClassLoader().getResourceAsStream(file);
            return Optional.ofNullable(new BufferedReader(new InputStreamReader(is)).readLine());
        }
        catch (Exception ignored) {
            ignored.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> getCompilationTime() {
        return readLineFrom("generated/compilation-time").map(LocalDateTime::parse);
    }

    public static Optional<String> getCommitId() {
        return readLineFrom("generated/commit-id");
    }
}

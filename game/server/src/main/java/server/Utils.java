package server;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.Closeable;

@UtilityClass
public class Utils {
    @SneakyThrows
    void close(Closeable closeable) {
        closeable.close();
    }
}

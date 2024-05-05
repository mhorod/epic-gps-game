package soturi.content;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SanityChecker {
    private SanityChecker() {
        throw new RuntimeException();
    }
    private static final Set<String> alreadyChecked = Collections.synchronizedSet(new HashSet<>());

    public static void checkEncodingAndReport() {
        if (!alreadyChecked.add("checkEncodingAndReport()"))
            return;

        byte[] actual = "Żółć".getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[]{-59, -69, -61, -77, -59, -126, -60, -121};
        if (Arrays.equals(actual, expected))
            return;

        System.err.println(
            "Something about the system you are running,\n" +
            "this JAR, gradle instance used to build it,\n" +
            "or this file encoding is beyond broken, have fun :)\n" +
            "Actual:   " + Arrays.toString(actual) + "\n" +
            "Expected: " + Arrays.toString(expected) + "\n"
        );
    }

    public static void checkResourceAndReport(String resource) {
        if (!alreadyChecked.add(resource))
            return;
        if (SanityChecker.class.getClassLoader().getResource(resource) != null)
            return;

        System.err.println(
            "Resource: \"" + resource + "\" is missing. \n" +
            "(it should be placed inside classpath before compilation)\n"
        );
    }
}

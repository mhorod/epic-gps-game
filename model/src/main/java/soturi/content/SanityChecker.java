package soturi.content;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SanityChecker {
    private SanityChecker() {
        throw new RuntimeException();
    }
    private static final Set<String> alreadyChecked = Collections.synchronizedSet(new HashSet<>());

    private static final byte[] ACTUAL_ENC = "Żółć".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EXPECTED_ENC = new byte[]{-59, -69, -61, -77, -59, -126, -60, -121};
    private static final boolean ENCODING_OK = Arrays.equals(ACTUAL_ENC, EXPECTED_ENC);

    public static void checkEncodingAndReport() {
        if (!alreadyChecked.add("checkEncodingAndReport()") || ENCODING_OK)
            return;

        System.err.println(
            "Something about the system you are running,\n" +
            "this JAR, gradle instance used to build it,\n" +
            "or this file encoding is beyond broken, have fun :)\n" +
            "Actual:   " + Arrays.toString(ACTUAL_ENC) + "\n" +
            "Expected: " + Arrays.toString(EXPECTED_ENC) + "\n"
        );
    }

    public static String fixEnc(String str) {
        if (ENCODING_OK)
            return str;
        checkEncodingAndReport();

        try {
            return new String(
                str.getBytes(Charset.forName("Cp1252")),
                StandardCharsets.UTF_8
            );
        }
        catch (UnsupportedCharsetException exception) {
            return str;
        }
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

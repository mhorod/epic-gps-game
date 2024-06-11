package gps.tracker;

import android.os.Build;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import soturi.common.Jackson;
import soturi.common.VersionInfo;

public class NetworkLogger {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final String LOG_URL = "https://soturi.online/post-log";

    public static String playerName;

    private static void reportMapBlocking(Map<String, Object> mp) {
        mp.put("compilationTime", VersionInfo.compilationTime);
        mp.put("commitId", VersionInfo.commitId);
        mp.put("Build.VERSION.SDK_INT", Build.VERSION.SDK_INT);
        mp.put("playerName", playerName);
        mp.put("time", LocalDateTime.now());

        try {
            String json = Jackson.mapper.writeValueAsString(mp);

            Request request = new Request.Builder()
                    .url(LOG_URL)
                    .post(RequestBody.create(json, JSON))
                    .build();
            OK_HTTP_CLIENT.newCall(request).execute().close();
        }
        catch (Throwable ignored) {
        }
    }

    public static void reportThrowable(Throwable e) {
        reportThrowableFromThread(Thread.currentThread(), e);
    }

    public static void reportThrowableFromThread(Thread t, Throwable e) {
        new Thread(() -> {
            Map<String, Object> mp = new HashMap<>();
            mp.put("thread", t == null ? null : t.getName());
            mp.put("throwable", e);
            mp.put("throwableClass", e == null ? null : e.getClass().getName());
            reportMapBlocking(mp);
        }).start();
    }
}

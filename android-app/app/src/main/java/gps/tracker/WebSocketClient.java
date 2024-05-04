package gps.tracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.androidrecord.AndroidRecordModule;

import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServer;
import soturi.model.messages_to_server.MessageToServerFactory;
import soturi.model.messages_to_server.UpdateRealPosition;

public class WebSocketClient extends WebSocketListener {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new AndroidRecordModule());
    private final MessageToClientHandler handler;
    public volatile String userName = "helloall2", userPassword = "password";
    private volatile WebSocket webSocket = null;
    private volatile Position lastPosition = null;

    public WebSocketClient(MessageToClientHandler handler) {
        this.handler = handler;
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    private void ensureSocketOpened() {
        if (webSocket != null)
            return;
        if (lastPosition == null)
            throw new RuntimeException("position is not known");

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(250, TimeUnit.MILLISECONDS)
                .build();

        System.err.println("dziem dobry");

        Request request = new Request.Builder()
                .header("epic-name", userName)
                .header("epic-password", userPassword)
                .header("epic-latitude", String.valueOf(lastPosition.latitude()))
                .header("epic-longitude", String.valueOf(lastPosition.longitude()))
                .url("ws://soturi.online:8080/ws/game")
                .build();

        System.err.println("dziem dobry2");

        webSocket = client.newWebSocket(request, this);

        System.err.println("dziem dobry3");
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
    }

    @Override
    @SneakyThrows
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("[REC] " + text);
        MessageToClient message = objectMapper.readValue(text, MessageToClient.class);
        System.out.println("[REC] " + message);
        message.process(handler);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        throw new RuntimeException();
    }

    @Override
    public void onClosing(WebSocket closedSocket, int code, String reason) {
        if (webSocket == closedSocket)
            webSocket = null;
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

    @SneakyThrows
    public void sendMessage(MessageToServer message) {
        System.out.println("[SND] " + message);

        if (message instanceof UpdateRealPosition updateRealPosition)
            lastPosition = updateRealPosition.position();
        ensureSocketOpened();

        String asText = objectMapper.writeValueAsString(message);
        if (!webSocket.send(asText))
            webSocket = null;
    }

    public MessageToServerFactory send() {
        return new MessageToServerFactory(this::sendMessage);
    }
}

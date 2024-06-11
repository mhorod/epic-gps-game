package gps.tracker;

import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import soturi.common.Jackson;
import soturi.common.VersionInfo;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServer;
import soturi.model.messages_to_server.MessageToServerFactory;
import soturi.model.messages_to_server.UpdateRealPosition;

public class WebSocketClient extends WebSocketListener {
    private final MessageToClientHandler handler;
    private final String urlPrefix;
    public volatile String userName = "helloall2", userPassword = "password";
    private volatile WebSocket webSocket = null;
    private volatile Position lastPosition = null;

    public WebSocketClient(MessageToClientHandler handler, String userName, String userPassword, boolean connectToDev) {
        NetworkLogger.playerName = userName;
        this.handler = handler;
        this.urlPrefix = connectToDev ? "dev." : "";
        this.userName = userName;
        this.userPassword = userPassword;
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
                .readTimeout(1250, TimeUnit.MILLISECONDS)
                .build();

        System.err.println("dziem dobry");

        Request request = new Request.Builder()
                .header("epic-name", userName)
                .header("epic-password", userPassword)
                .header("epic-latitude", String.valueOf(lastPosition.latitude()))
                .header("epic-longitude", String.valueOf(lastPosition.longitude()))
                .header("epic-version", String.valueOf(VersionInfo.compilationTime))
                .url("wss://" + urlPrefix + "soturi.online/ws/game")
                .build();

        System.err.println("dziem dobry2");

        webSocket = client.newWebSocket(request, this);

        System.err.println("dziem dobry3");
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        System.out.println("OPEN: " + response.code());
    }

    @Override
    @SneakyThrows
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("[REC] " + text);
        MessageToClient message = Jackson.mapper.readValue(text, MessageToClient.class);
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
        handler.disconnect();
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

        String asText = Jackson.mapper.writeValueAsString(message);
        if (!webSocket.send(asText))
            webSocket = null;
    }

    public MessageToServerFactory send() {
        return new MessageToServerFactory(this::sendMessage);
    }
}

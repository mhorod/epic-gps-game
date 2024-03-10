package gps.tracker;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import lombok.SneakyThrows;
import model.messages_to_client.MessageToClientHandler;
import model.messages_to_server.MessageToServer;
import model.messages_to_server.MessageToServerFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient extends WebSocketListener {
    private final WebSocket webSocket;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    private final MessageToClientHandler handler;

    public WebSocketClient(MessageToClientHandler handler) {
        this.handler = handler;

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .header("param", "value")
                .url("ws://52.158.44.176:8080/ws/game")
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        send().loginInfo("apka", "mudd");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        System.out.println("MESSAGE: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        System.out.println("MESSAGE: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        System.out.println("CLOSE: " + code + " " + reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        t.printStackTrace();
    }

    public MessageToServerFactory send() {
        return new MessageToServerFactory(new Consumer<>() {
            @Override
            @SneakyThrows
            public void accept(MessageToServer message) {
                System.out.println("[SND] " + message);

                String asText = objectMapper.writeValueAsString(message);
                System.out.println("[SND] " + asText);

                webSocket.send(asText);
            }
        });
    }
}

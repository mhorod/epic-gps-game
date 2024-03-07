package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSocket
public class HelloWorldClient extends AbstractWebSocketHandler implements WebSocketConfigurer {
    private final ObjectMapper mapper;

    @SneakyThrows
    HelloWorldClient(ObjectMapper mapper) {
        this.mapper = mapper;

        Monster smok = new Monster("Smok Wawelski", 42, new Position(50.05432952579264, 19.935962760063525));
        MonstersMessage monsters = new MonstersMessage(List.of(smok));

        log.info("lista ze smokiem: {}", mapper.writeValueAsString(monsters));


        String json = "{\"type\":\".MonstersMessage\",\"monsters\":[{\"name\":\"Smok Wawelski\",\"lvl\":42,\"position\":{\"latitude\":50.05432952579264,\"longitude\":19.935962760063525}}]}";
        MonstersMessage deserialized = (MonstersMessage) mapper.readValue(json, Message.class);
        log.info("deserialized smok latitude: {}", deserialized.monsters().get(0).position().latitude());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("{}", session);
    }

    @SneakyThrows
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("{} {}", session, message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.info("{} {}", session, message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("{} {}", session, status);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("init {}", registry);
        registry.addHandler(this, "/game");
    }
}

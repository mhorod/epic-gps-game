package server;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.swing.text.Position;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSocket
public class HelloWorldClient extends AbstractWebSocketHandler implements WebSocketConfigurer {
    private final ObjectMapper mapper;

    HelloWorldClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    record Position(double latitude, double longitude) {}
    record Monster(String name, int lvl, Position position) {}

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("{}", session);
    }

    @SneakyThrows
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("{} {}", session, message);
        List<Monster> monsters = List.of(new Monster("Smok Wawelski", 42, new Position(50.05432952579264, 19.935962760063525)));
        session.sendMessage(new TextMessage(mapper.writeValueAsString(monsters)));
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

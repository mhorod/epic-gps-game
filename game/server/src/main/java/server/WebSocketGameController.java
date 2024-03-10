package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.messages_to_client.MessageToClient;
import model.messages_to_client.MessageToClientFactory;
import model.messages_to_client.MessageToClientHandler;
import model.messages_to_server.MessageToServer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
@Component
public final class WebSocketGameController extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final GameService gameService;

    private MessageToClientHandler sendToSession(WebSocketSession session) {
        return new MessageToClientFactory(new Consumer<>() {
            @Override
            @SneakyThrows
            public void accept(MessageToClient messageToClient) {
                String authName = (String) session.getAttributes().get("auth-name");
                log.info("[ TO ] {} [MSG] {}", authName, messageToClient);

                String asText = mapper.writeValueAsString(messageToClient);
                session.sendMessage(new TextMessage(asText));
            }
        });
    }

    @Override
    @SneakyThrows
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        MessageToServer message = mapper.readValue(textMessage.getPayload(), MessageToServer.class);

        String authName = (String) session.getAttributes().get("auth-name");
        log.info("[FROM] {} [MSG] {}", authName, message);

        if (authName == null) {
            log.warn("achievement unlocked: how did we get here");
            throw new RuntimeException();
        }

        message.process(gameService.sendTo(authName));
    }

    @Override
    @SneakyThrows
    public void afterConnectionEstablished(WebSocketSession session) {
        HttpHeaders headers = session.getHandshakeHeaders();
        MessageToClientHandler sender = sendToSession(session);

        String name = headers.getFirst("epic-name");
        String password = headers.getFirst("epic-password");

        // TODO hash password
        if (gameService.login(name, password, sender))
            session.getAttributes().put("auth-name", name);
        else
            session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String authName = (String) session.getAttributes().get("auth-name");
        log.info("[CLOSE] {} [STATUS] {}", authName, status);

        if (authName != null)
            gameService.logout(authName);
    }
}

package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.messages_to_client.MessageToClient;
import model.messages_to_client.MessageToClientFactory;
import model.messages_to_server.MessageToServer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
@Component
public final class WebSocketGameController extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final GameService gameService;

    private MessageToClientFactory sendToSession(WebSocketSession session) {
        return new MessageToClientFactory(new Consumer<>() {
            @Override
            @SneakyThrows
            public void accept(MessageToClient messageToClient) {
                String authName = (String) session.getAttributes().get("authName");
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

        String authName = (String) session.getAttributes().get("authName");
        log.info("[FROM] {} [MSG] {}", authName, message);

        if (authName != null) {
            message.process(gameService.sendTo(authName));
            return;
        }

        Optional<String> loggedName = gameService.login(message, sendToSession(session));
        loggedName.ifPresentOrElse(
            name -> session.getAttributes().put("authName", name),
            () -> Utils.close(session)
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String authName = (String) session.getAttributes().get("authName");
        log.info("[CLOSE] {} [STATUS] {}", authName, status);

        if (authName != null)
            gameService.logout(authName);
    }
}

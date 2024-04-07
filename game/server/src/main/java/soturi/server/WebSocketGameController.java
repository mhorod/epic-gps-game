package soturi.server;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServer;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@AllArgsConstructor
@Component
public final class WebSocketGameController extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final GameService gameService;

    private static void close(WebSocketSession session) {
        try {
            session.close();
        }
        catch (IOException exception) {
            throw new RuntimeException("WebSocketSession::close() can throw !?", exception);
        }
    }

    private MessageToClientHandler sendToSession(WebSocketSession session) {
        return new MessageToClientFactory(message -> Thread.ofPlatform().daemon().start(() -> {
            synchronized (session) {
                String wsName = (String) session.getAttributes().get("ws-name");
                log.info("[ TO ] {} [MSG] {}", wsName, message);

                if (wsName == null) {
                    log.error("this should never happen (wsName == null)");
                    close(session);
                    return;
                }

                try {
                    String asText = mapper.writeValueAsString(message);
                    session.sendMessage(new TextMessage(asText));
                }
                catch (JacksonException exception) {
                    log.error("jackson exception", exception);
                    close(session);
                }
                catch (IOException ignored) {
                    log.info("exception thrown while sending to {}", wsName);
                    close(session);
                }
            }
        }).start());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        synchronized (session) {
            try {
                MessageToServer message = mapper.readValue(textMessage.getPayload(), MessageToServer.class);

                String wsName = (String) session.getAttributes().get("ws-name");
                log.info("[FROM] {} [MSG] {}", wsName, message);

                if (wsName == null) {
                    log.error("achievement unlocked: how did we get here?");
                    close(session);
                    return;
                }

                message.process(gameService.receiveFrom(wsName));
            }
            catch (JacksonException exception) {
                log.info("jackson exception", exception);
                close(session);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        synchronized (session) {
            HttpHeaders headers = session.getHandshakeHeaders();
            MessageToClientHandler sender = sendToSession(session);

            String name = headers.getFirst("epic-name");
            String password = headers.getFirst("epic-password");

            session.getAttributes().put("ws-name", name);

            // TODO hash password
            if (!gameService.login(name, password, sender)) {
                log.info("unsuccessful authentication");
                close(session);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        synchronized (session) {
            String wsName = (String) session.getAttributes().get("ws-name");
            log.info("[CLOSE] {} [STATUS] {}", wsName, status);

            if (wsName != null)
                gameService.logout(wsName);
        }
    }
}

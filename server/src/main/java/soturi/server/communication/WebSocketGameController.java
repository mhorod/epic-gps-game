package soturi.server.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import soturi.server.GameService;

@Slf4j
@AllArgsConstructor
@Component
public class WebSocketGameController extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final GameService gameService;

    private Connection getConnection(WebSocketSession session) {
        synchronized (session) {
            return (Connection) session.getAttributes().computeIfAbsent(
                "epic-connection",
                ignored -> new Connection(session, gameService, mapper)
            );
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        synchronized (session) {
            getConnection(session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        synchronized (session) {
            getConnection(session).handleTextMessage(textMessage.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        synchronized (session) {
            getConnection(session).close();
        }
    }
}

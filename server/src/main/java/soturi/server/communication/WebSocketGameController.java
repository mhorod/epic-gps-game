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
public final class WebSocketGameController extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        synchronized (session) {
            Connection connection = new Connection(session, gameService, mapper);
            session.getAttributes().put("ws-connection", connection);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        synchronized (session) {
            Connection connection = (Connection) session.getAttributes().get("ws-connection");
            connection.handleTextMessage(textMessage.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        synchronized (session) {
            Connection connection = (Connection) session.getAttributes().get("ws-connection");
            connection.close();
        }
    }
}

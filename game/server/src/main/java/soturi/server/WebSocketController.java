package soturi.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import soturi.model.EnemyId;
import soturi.model.Item;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_server.MessageToServer;
import soturi.model.messages_to_server.MessageToServerHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketController extends TextWebSocketHandler implements WebSocketConfigurer {
    private final ObjectMapper mapper;
    private final PlayerService playerService;

    private final Map<String, String> sessionIdToPlayerName = new LinkedHashMap<>();

    WebSocketController(ObjectMapper mapper, PlayerService playerService) {
        this.mapper = mapper;
        this.playerService = playerService;
    }

    MessageToClientFactory sendToSession(WebSocketSession session) {
        return new MessageToClientFactory(new Consumer<>() {
            @Override
            @SneakyThrows
            public void accept(MessageToClient messageToClient) {
                log.info("[ TO ] {} [MSG] {}", sessionIdToPlayerName.get(session.getId()), messageToClient);

                String asText = mapper.writeValueAsString(messageToClient);
                session.sendMessage(new TextMessage(asText));
            }
        });
    }

    @Override
    @SneakyThrows
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        MessageToServer message = mapper.readValue(textMessage.getPayload(), MessageToServer.class);
        MessageToClientFactory reply = sendToSession(session);

        log.info("[FROM] {} [MSG] {}", sessionIdToPlayerName.get(session.getId()), message);

        // FIXME
        // the authorization code is... just is
        // it will be removed one day

        message.process(new MessageToServerHandler() {
            @Override
            public void attackEnemy(EnemyId enemyId) {
                reply.error("not supported");
            }

            @Override
            public void equipItem(Item item) {
                reply.error("not supported");
            }

            @Override
            public void loginInfo(String name, String password) {
                if (sessionIdToPlayerName.containsKey(session.getId())) {
                    reply.error("you are already logged in");
                    return;
                }
                if (playerService.getAllLoggedIn().contains(name)) {
                    reply.error("this player is already logged in");
                    return;
                }
                if (!password.equals("mud")) {
                    reply.error("incorrect password");
                    return;
                }
                sessionIdToPlayerName.put(session.getId(), name);
                playerService.login(name, reply);
            }

            @Override
            public void unequipItem(Item item) {
                reply.error("not supported");
            }

            @Override
            public void updateLookingPosition(Position position) {
                if (!sessionIdToPlayerName.containsKey(session.getId())) {
                    reply.error("log in first");
                    return;
                }
            }

            @Override
            public void updateRealPosition(Position position) {
                if (!sessionIdToPlayerName.containsKey(session.getId())) {
                    reply.error("log in first");
                    return;
                }
                playerService.updatePosition(sessionIdToPlayerName.get(session.getId()), position);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("close: {} {}", session, status);

        String playerName = sessionIdToPlayerName.get(session.getId());
        sessionIdToPlayerName.remove(session.getId());
        playerService.logout(playerName);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this, "/soturi").setAllowedOrigins("*");
    }
}

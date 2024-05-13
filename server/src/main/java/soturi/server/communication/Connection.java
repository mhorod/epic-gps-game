package soturi.server.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import soturi.model.Position;
import soturi.model.messages_to_client.Disconnect;
import soturi.model.messages_to_client.EnemiesAppear;
import soturi.model.messages_to_client.EnemiesDisappear;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_client.Ping;
import soturi.model.messages_to_server.MessageToServer;
import soturi.server.GameService;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is thread safe
 */
@Slf4j
public class Connection {
    private final WebSocketSession session;
    private final GameService gameService;
    private final ObjectMapper objectMapper;

    private final BlockingQueue<MessageToClient> queue = new LinkedBlockingQueue<>();

    private volatile String authorizedUser = null;
    private volatile boolean closed = false;
    private volatile Instant lastReceived = Instant.now();

    private static Position positionFromStrings(String latitude, String longitude) {
        try {
            double latitudeAsDouble = Double.parseDouble(latitude);
            double longitudeAsDouble = Double.parseDouble(longitude);
            return new Position(latitudeAsDouble, longitudeAsDouble);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Connection(WebSocketSession session, GameService gameService, ObjectMapper objectMapper) {
        synchronized (session) {
            this.session = session;
            this.gameService = gameService;
            this.objectMapper = objectMapper;

            HttpHeaders headers = session.getHandshakeHeaders();

            String user = headers.getFirst("epic-name");
            String password = headers.getFirst("epic-password");
            String latitude = headers.getFirst("epic-latitude");
            String longitude = headers.getFirst("epic-longitude");

            Position position = positionFromStrings(latitude, longitude);
            MessageToClientHandler handler = new MessageToClientFactory(queue::add);

            if (gameService.login(user, password, position, handler))
                authorizedUser = user;
            else
                scheduleToClose();

            Thread.ofVirtual().start(this::work);
        }
    }

    public void scheduleToClose() {
        queue.add(new Disconnect());
    }

    public void close() {
        synchronized (session) {
            if (closed)
                return;
            closed = true;
            try {
                session.close();
            }
            catch (IOException exception) {
                log.error("WebSocketSession::close() can throw !?", exception);
            }
            if (authorizedUser != null)
                gameService.logout(authorizedUser);
        }
    }

    public void handleTextMessage(String message) {
        synchronized (session) {
            if (closed)
                return;
            if (authorizedUser == null) {
                scheduleToClose();
                return;
            }
            lastReceived = Instant.now();
            try {
                MessageToServer messageToServer = objectMapper.readValue(message, MessageToServer.class);
                log.info("[FROM] {} [MSG] {}", authorizedUser, messageToServer);
                messageToServer.process(gameService.receiveFrom(authorizedUser));
            }
            catch (JsonProcessingException jsonProcessingException) {
                log.error("user thinks he is funny", jsonProcessingException);
                close();
            }
        }
    }

    private boolean timeoutExceeded() {
        return Duration.between(lastReceived, Instant.now()).getSeconds() > 6;
    }

    private void work() {
        while (!closed) {
            MessageToClient messageToClient;
            try {
                messageToClient = BlockingQueuePoll.poll(queue, 4);
                if (messageToClient == null)
                    messageToClient = new Ping();
            }
            catch (InterruptedException interruptedException) {
                log.error("this should not happen", interruptedException);
                close();
                break;
            }
            synchronized (session) {
                if (closed || timeoutExceeded() || messageToClient instanceof Disconnect) {
                    close();
                    break;
                }

                if (messageToClient instanceof EnemiesAppear appear)
                    log.info("[ TO ] {} [MSG] EnemiesAppear[#={}]", authorizedUser, appear.enemies().size());
                else if (messageToClient instanceof EnemiesDisappear disappear)
                    log.info("[ TO ] {} [MSG] EnemiesDisappear[#={}]", authorizedUser, disappear.enemyIds().size());
                else
                    log.info("[ TO ] {} [MSG] {}", authorizedUser, messageToClient);

                try {
                    String payload = objectMapper.writeValueAsString(messageToClient);
                    session.sendMessage(new TextMessage(payload));
                }
                catch (JsonProcessingException jsonProcessingException) {
                    log.error("this should not happen", jsonProcessingException);
                    close();
                    break;
                }
                catch (IOException | IllegalStateException exception) {
                    close();
                    break;
                }
            }
        }
    }

}

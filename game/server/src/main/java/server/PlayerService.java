package server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.Enemy;
import model.EnemyId;
import model.Player;
import model.Position;
import model.messages_to_client.MessageToClientFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class PlayerService {
    private final Map<String, PersistentPlayer> playerMap = new LinkedHashMap<>();
    private final Map<String, MessageToClientFactory> senders = new LinkedHashMap<>();
    private final Map<String, Position> playerLocations = new LinkedHashMap<>();

    void login(String player, MessageToClientFactory factory) {
        senders.put(player, factory);
        if (!playerMap.containsKey(player))
            playerMap.put(player, new PersistentPlayer(player));

        factory.enemyAppears(new Enemy(
                "Smok Wawelski",
                42,
                new EnemyId(0),
                new Position(50.053916238510816, 19.934922086710337)
        ));
        factory.enemyAppears(new Enemy(
                "Student TCS",
                3,
                new EnemyId(1),
                new Position(50.03066707260884, 19.906860304304956)
        ));
    }
    void logout(String player) {
        senders.remove(player);
        playerLocations.remove(player);
    }
    void sendUpdateFor(String playerName) {
        Player player = playerMap.get(playerName).toPlayer();
        Position position = playerLocations.get(playerName);

        for (String name : getAllLoggedIn()) if (!name.equals(playerName))
            sendTo(name).playerUpdate(player, position);
        sendTo(playerName).meUpdate(player);
    }
    void updatePosition(String player, Position position) {
        playerLocations.put(player, position);
        sendUpdateFor(player);
    }
    void gainXp(String player, long xp) {
        playerMap.put(player, playerMap.get(player).gainXp(xp));
        sendUpdateFor(player);
    }
    MessageToClientFactory sendTo(String player) {
        return senders.get(player);
    }

    Collection<String> getAllLoggedIn() {
        return senders.keySet();
    }
    Collection<MessageToClientFactory> getAllSenders() {
        return senders.values();
    }
    Collection<Position> getAllLocations() {
        return playerLocations.values();
    }
}

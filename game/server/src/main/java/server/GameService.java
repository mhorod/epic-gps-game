package server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Enemy;
import model.EnemyId;
import model.FightResult;
import model.Item;
import model.Player;
import model.PlayerWithPosition;
import model.Position;
import model.Result;
import model.messages_to_client.MessageToClientHandler;
import model.messages_to_server.MessageToServerHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public final class GameService {
    private final PlayerRepository repository;
    private final Map<String, PlayerSession> sessions;
    private final Map<String, MessageToClientHandler> observers;
    private final Map<EnemyId, Enemy> enemies;
    private final GameUtility gameUtility;
    private final FightSimulator fightSimulator;

    @Scheduled(fixedDelayString = "${give-free-xp.delay-in-milliseconds}")
    void giveFreeXp() {
        log.debug("giveFreeXp()");
        if (gameUtility.giveFreeXpAmount == 0)
            return;
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            entity.addXp(gameUtility.giveFreeXpAmount);
            repository.save(entity);
            sendUpdatesFor(player);
        }
    }

    @Scheduled(fixedDelayString = "${spawn-enemy.delay-in-milliseconds}")
    void spawnEnemy() {
        log.debug("spawnEnemy()");
        List<Enemy> enemyList = enemies.values().stream().toList();
        Stream<Optional<PlayerWithPosition>> playerStream = sessions.entrySet().stream().map(kv -> {
            Position position = kv.getValue().getPosition();
            if (position == null)
                return Optional.empty();
            PlayerEntity entity = repository.findByName(kv.getKey()).orElseThrow();
            Player player = gameUtility.getPlayerFromEntity(entity);
            return Optional.of(new PlayerWithPosition(player, position));
        });
        List<PlayerWithPosition> players = playerStream.flatMap(Optional::stream).toList();

        gameUtility.generateOrdinaryEnemy(enemyList, players).ifPresent(this::registerEnemy);
    }

    void registerEnemy(Enemy enemy) {
        if (enemies.put(enemy.enemyId(), enemy) != null)
            throw new RuntimeException();
        for (var session : sessions.values())
            session.getSender().enemyAppears(enemy);
        for (var sender : observers.values())
            sender.enemyAppears(enemy);
    }

    void unregisterEnemy(EnemyId enemyId) {
        if (enemies.remove(enemyId) == null)
            throw new RuntimeException();
        for (var session : sessions.values())
            session.getSender().enemyDisappears(enemyId);
        for (var sender : observers.values())
            sender.enemyDisappears(enemyId);
    }

    void sendUpdatesFor(@NonNull String playerName) {
        PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
        Player playerData = gameUtility.getPlayerFromEntity(playerEntity);
        PlayerSession playerSession = sessions.get(playerName);
        Position playerPosition = playerSession.getPosition();

        for (var kv : sessions.entrySet()) if (!kv.getKey().equals(playerName))
            kv.getValue().getSender().playerUpdate(playerData, playerPosition);
        playerSession.getSender().meUpdate(playerData);

        for (var sender : observers.values())
            sender.playerUpdate(playerData, playerPosition);
    }

    private void processAttack(String playerName, EnemyId enemyId) {
        PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
        Player playerData = gameUtility.getPlayerFromEntity(playerEntity);
        PlayerSession playerSession = sessions.get(playerName);
        Position playerPosition = playerSession.getPosition();

        Enemy enemy = enemies.get(enemyId);
        if (enemy == null) {
            playerSession.getSender().error("this enemy does not exist");
            return;
        }
        if (enemy.position().distance(playerPosition) > gameUtility.fightingMaxDistInMeters) {
            playerSession.getSender().error("this enemy is too far");
            return;
        }

        FightResult result = fightSimulator.simulateFight(playerData, enemy);
        playerSession.getSender().fightResult(result.result(), enemyId);
        playerEntity.applyFightResult(result);
        repository.save(playerEntity);

        if (result.result() == Result.WON)
            unregisterEnemy(enemyId);
    }

    MessageToServerHandler sendTo(@NonNull String playerName) {
        @NonNull PlayerSession session = sessions.get(playerName);
        MessageToClientHandler sender = session.getSender();

        return new MessageToServerHandler() {
            @Override
            public void attackEnemy(EnemyId enemyId) {
                processAttack(playerName, enemyId);
            }

            @Override
            public void equipItem(Item item) {
                sender.error("not supported");
            }

            @Override
            public void unequipItem(Item item) {
                sender.error("not supported");
            }

            @Override
            public void updateLookingPosition(Position position) {
                session.setLooking(position);
            }

            @Override
            public void updateRealPosition(Position position) {
                session.setPosition(position);
                sendUpdatesFor(playerName);
            }
        };
    }

    boolean login(String name, String hashedPassword, @NonNull MessageToClientHandler sender) {
        if (name == null || name.isEmpty() || hashedPassword == null) {
            sender.error("null data passed");
            return false;
        }
        PlayerEntity entity = repository.findByName(name).orElseGet(
            () -> repository.save(new PlayerEntity(name, hashedPassword))
        );
        if (!hashedPassword.equals(entity.getHashedPassword())) {
            sender.error("incorrect password passed");
            return false;
        }
        if (sessions.containsKey(name)) {
            sender.error("this player is already logged in");
            return false;
        }
        sessions.put(name, new PlayerSession(sender));
        sendUpdatesFor(name);
        return true;
    }

    void logout(@NonNull String playerName) {
        if (sessions.remove(playerName) == null)
            throw new RuntimeException();
    }

    void addObserver(String id, MessageToClientHandler observer) {
        if (observers.put(id, observer) != null)
            throw new RuntimeException();
    }

    void remObserver(String id) {
        if (observers.remove(id) == null)
            throw new RuntimeException();
    }
}

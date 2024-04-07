package soturi.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.FightResult;
import soturi.model.Item;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServerHandler;
import soturi.server.geo.GeoManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public final class GameService {
    private final Map<String, PlayerSession> sessions = new LinkedHashMap<>();
    private final Map<String, MessageToClientHandler> observers = new LinkedHashMap<>();
    private final Map<EnemyId, Enemy> enemies = new LinkedHashMap<>();
    private final PlayerRepository repository;
    private final GameUtility gameUtility;
    private final GeoManager geoManager;
    private final FightSimulator fightSimulator;

    public void stopAndDisconnectAll() {
        while (!sessions.isEmpty())
            logout(sessions.entrySet().iterator().next().getKey());
        while (!observers.isEmpty())
            remObserver(observers.entrySet().iterator().next().getKey());
        while (!enemies.isEmpty())
            unregisterEnemy(enemies.entrySet().iterator().next().getKey());
    }

    public synchronized List<Enemy> getEnemies() {
        return enemies.values().stream().toList();
    }

    public synchronized List<PlayerWithPosition> getAllPlayers() {
        return sessions.keySet().stream()
            .map(repository::findByName)
            .flatMap(Optional::stream)
            .map(gameUtility::getPlayerFromEntity)
            .map(p -> new PlayerWithPosition(p, sessions.get(p.name()).getPosition()))
            .toList();
    }

    public synchronized List<PlayerWithPosition> getPlayers() { // only those with position
        return getAllPlayers().stream()
            .filter(playerWithPosition -> playerWithPosition.position() != null)
            .toList();
    }

    @Scheduled(fixedDelayString = "${give-free-xp.delay-in-milliseconds}")
    private synchronized void giveFreeXp() {
        log.info("giveFreeXp() started");

        if (gameUtility.giveFreeXpAmount == 0)
            return;
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            entity.addXp(gameUtility.giveFreeXpAmount);
            repository.save(entity);
            sendUpdatesFor(player);
        }

        log.info("giveFreeXp() exited");
    }

    @Scheduled(fixedDelayString = "${spawn-enemy.delay-in-milliseconds}")
    private synchronized void spawnEnemy() {
        log.info("spawnEnemy() started");

        List<Enemy> enemyList = enemies.values().stream().toList();
        gameUtility.generateOrdinaryEnemy(enemyList, getPlayers()).ifPresent(this::registerEnemy);

        log.info("spawnEnemy() exited");
    }

    private synchronized void registerEnemy(Enemy enemy) {
        if (enemies.put(enemy.enemyId(), enemy) != null)
            throw new RuntimeException();
        for (var session : sessions.values())
            session.getSender().enemyAppears(enemy);
        for (var sender : observers.values())
            sender.enemyAppears(enemy);
    }

    private synchronized void unregisterEnemy(EnemyId enemyId) {
        if (enemies.remove(enemyId) == null)
            throw new RuntimeException();
        for (var session : sessions.values())
            session.getSender().enemyDisappears(enemyId);
        for (var sender : observers.values())
            sender.enemyDisappears(enemyId);
    }

    private synchronized void sendUpdatesFor(@NonNull String playerName) {
        PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
        Player playerData = gameUtility.getPlayerFromEntity(playerEntity);
        PlayerSession playerSession = sessions.get(playerName);
        Position playerPosition = playerSession.getPosition();

        for (var kv : sessions.entrySet())
            if (!kv.getKey().equals(playerName))
                kv.getValue().getSender().playerUpdate(playerData, playerPosition);
        playerSession.getSender().meUpdate(playerData);

        for (var sender : observers.values())
            sender.playerUpdate(playerData, playerPosition);
    }

    private synchronized void processAttackEnemy(String playerName, EnemyId enemyId) {
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

    public synchronized MessageToServerHandler receiveFrom(@NonNull String playerName) {
        @NonNull PlayerSession session = sessions.get(playerName);
        MessageToClientHandler sender = session.getSender();

        return new MessageToServerHandler() {
            @Override
            public void attackEnemy(EnemyId enemyId) {
                synchronized (GameService.this) {
                    processAttackEnemy(playerName, enemyId);
                }
            }

            @Override
            public void equipItem(Item item) {
                synchronized (GameService.this) {
                    sender.error("not supported");
                }
            }

            @Override
            public void unequipItem(Item item) {
                synchronized (GameService.this) {
                    sender.error("not supported");
                }
            }

            @Override
            public void updateLookingPosition(Position position) {
                synchronized (GameService.this) {
                    session.setLooking(position);
                }
            }

            @Override
            public void updateRealPosition(Position position) {
                synchronized (GameService.this) {
                    session.setPosition(position);
                    sendUpdatesFor(playerName);
                }
            }
        };
    }

    private synchronized void doLogin(String name, String hashedPassword, MessageToClientHandler sender) {
        log.info("doLogin({})", name);
        sessions.put(name, new PlayerSession(sender));
        sendUpdatesFor(name);

        for (var kv : sessions.entrySet()) if (!kv.getKey().equals(name))
            sender.playerUpdate(
                gameUtility.getPlayerFromEntity(repository.findByName(kv.getKey()).orElseThrow()),
                kv.getValue().getPosition()
            );
        for (Enemy enemy : enemies.values())
            sender.enemyAppears(enemy);
    }

    public synchronized boolean login(String name, String hashedPassword, @NonNull MessageToClientHandler sender) {
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

        doLogin(name, hashedPassword, sender);
        return true;
    }

    public synchronized void logout(@NonNull String playerName) {
        log.info("logout({})", playerName);
        if (sessions.remove(playerName) == null)
            throw new RuntimeException();
        for (var session : sessions.values())
            session.getSender().playerDisappears(playerName);
        for (var observer : observers.values())
            observer.playerDisappears(playerName);
    }

    public synchronized void addObserver(String id, MessageToClientHandler observer) {
        if (observers.put(id, observer) != null)
            throw new RuntimeException();

        for (var kv : sessions.entrySet())
            observer.playerUpdate(
                gameUtility.getPlayerFromEntity(repository.findByName(kv.getKey()).orElseThrow()),
                kv.getValue().getPosition()
            );
        for (Enemy enemy : enemies.values())
            observer.enemyAppears(enemy);
    }

    public synchronized void remObserver(String id) {
        if (observers.remove(id) == null)
            throw new RuntimeException();
    }
}

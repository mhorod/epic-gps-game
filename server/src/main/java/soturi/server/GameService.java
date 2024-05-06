package soturi.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import soturi.content.GameRegistry;
import soturi.content.ItemRegistry;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.ItemId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.FightResult;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServerHandler;
import soturi.server.geo.MonsterManager;

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
    private final PlayerRepository repository;
    private final Config config;
    private final MonsterManager monsterManager;
    private final FightSimulator fightSimulator;

    private final GameRegistry gameRegistry = new GameRegistry();
    private final ItemRegistry itemRegistry = new ItemRegistry();

    public void kickAllPlayers() {
        while (!sessions.isEmpty())
            logout(sessions.entrySet().iterator().next().getKey());
    }

    public void kickAllObservers() {
        while (!observers.isEmpty())
            removeObserver(observers.entrySet().iterator().next().getKey());
    }

    public synchronized void unregisterAllEnemies() {
        for (Enemy enemy : getEnemies())
            unregisterEnemy(enemy.enemyId());
    }

    public synchronized void reloadDynamicConfig() {
        unregisterAllEnemies();
        monsterManager.reloadAreas();
    }

    public synchronized List<Enemy> getEnemies() {
        return monsterManager.getAllEnemies();
    }

    public synchronized List<PlayerWithPosition> getPlayers() {
        return sessions.keySet().stream()
            .map(repository::findByName)
            .flatMap(Optional::stream)
            .map(this::getPlayerFromEntity)
            .map(p -> new PlayerWithPosition(p, sessions.get(p.name()).getPosition()))
            .toList();
    }

    public synchronized void healPlayers() {
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            long missingHp = getPlayerFromEntity(entity).maxHp() - entity.getHp();
            long healed = (long) (missingHp * config.v.healFraction) + 1;
            healed = Math.max(0, Math.min(missingHp, healed));
            if (healed == 0)
                continue;
            entity.addHp(healed);
            repository.save(entity);
            sendUpdatesFor(player);
        }
    }


    private long secondCount = 0;
    private synchronized void doTickEverySecond() {
        secondCount++;

        if (config.v.giveFreeXpDelayInSeconds > 0 && secondCount % config.v.giveFreeXpDelayInSeconds == 0)
            giveFreeXp();
        if (config.v.spawnEnemyDelayInSeconds > 0 && secondCount % config.v.spawnEnemyDelayInSeconds == 0)
            spawnEnemies();
        if (config.v.healDelayInSeconds > 0 && secondCount % config.v.healDelayInSeconds == 0)
            healPlayers();
    }

    @Scheduled(fixedDelay = 1000)
    private synchronized void tickEverySecond() {
        doTickEverySecond();
    }

    private synchronized void giveFreeXp() {
        log.info("giveFreeXp() called");

        if (config.v.giveFreeXpAmount == 0)
            return;
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            entity.addXp(config.v.giveFreeXpAmount);
            repository.save(entity);
            sendUpdatesFor(player);
        }
    }

    private synchronized void spawnEnemies() {
        log.info("spawnEnemies() called");

        for (Enemy enemy : monsterManager.generateEnemies())
            registerEnemy(enemy);
    }

    public synchronized void registerEnemy(Enemy enemy) {
        monsterManager.registerEnemy(enemy);

        for (var session : sessions.values())
            session.getSender().enemyAppears(enemy);
        for (var sender : observers.values())
            sender.enemyAppears(enemy);
    }

    private synchronized void unregisterEnemy(EnemyId enemyId) {
        monsterManager.unregisterEnemy(enemyId);

        for (var session : sessions.values())
            session.getSender().enemyDisappears(enemyId);
        for (var sender : observers.values())
            sender.enemyDisappears(enemyId);
    }

    public synchronized Player getPlayerFromEntity(PlayerEntity entity) {
        int lvl = gameRegistry.getLvlFromXp(entity.getXp());
        long maxHp = lvl * 100L;
        long attack = lvl * 50L;
        long defense = lvl * 25L;

        return new Player(
            entity.getName(),
            lvl,
            entity.getXp(),
            entity.getHp(),
            maxHp,
            attack,
            defense,
            List.of(), // TODO list of items
            List.of()
        );
    }

    private synchronized void sendUpdatesFor(@NonNull String playerName) {
        PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
        Player playerData = getPlayerFromEntity(playerEntity);
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
        Player playerData = getPlayerFromEntity(playerEntity);
        PlayerSession playerSession = sessions.get(playerName);
        Position playerPosition = playerSession.getPosition();

        Enemy enemy = monsterManager.getEnemyMap().get(enemyId);
        if (enemy == null) {
            playerSession.getSender().error("this enemy does not exist");
            return;
        }
        if (enemy.position().distance(playerPosition) > config.v.fightingMaxDistInMeters) {
            playerSession.getSender().error("this enemy is too far");
            return;
        }

        FightResult result = fightSimulator.simulateFight(playerData, enemy);
        playerSession.getSender().fightResult(result.result(), result.lostHp(), result.enemyId(), result.loot());
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
            public void disconnect() {
                synchronized (GameService.this) {
                    sender.disconnect();
                }
            }

            @Override
            public void equipItem(ItemId itemId) {
                synchronized (GameService.this) {
                    sender.error("not supported");
                }
            }

            @Override
            public void unequipItem(ItemId itemId) {
                synchronized (GameService.this) {
                    sender.error("not supported");
                }
            }

            @Override
            public void ping() {
                synchronized (GameService.this) {
                    sender.pong();
                }
            }

            @Override
            public void pong() {
            }

            @Override
            public void updateLookingPosition(@NonNull Position position) {
                synchronized (GameService.this) {
                    session.setLooking(position);
                }
            }

            @Override
            public void updateRealPosition(@NonNull Position position) {
                synchronized (GameService.this) {
                    session.setPosition(position);
                    sendUpdatesFor(playerName);
                }
            }
        };
    }

    private synchronized void doLogin(@NonNull String name, @NonNull String password,
                                      @NonNull Position initialPosition, @NonNull MessageToClientHandler sender) {
        log.info("doLogin({})", name);
        sessions.put(name, new PlayerSession(sender, initialPosition, initialPosition));
        sendUpdatesFor(name);

        for (var kv : sessions.entrySet()) if (!kv.getKey().equals(name))
            sender.playerUpdate(
                getPlayerFromEntity(repository.findByName(kv.getKey()).orElseThrow()),
                kv.getValue().getPosition()
            );
        for (Enemy enemy : getEnemies())
            sender.enemyAppears(enemy);
    }

    public synchronized boolean login(String name, String password,
                                      Position initialPosition, @NonNull MessageToClientHandler sender) {
        if (name == null || name.isEmpty() || password == null || initialPosition == null) {
            sender.error("null data passed");
            return false;
        }
        PlayerEntity entity = repository.findByName(name).orElseGet(
            () -> repository.save(new PlayerEntity(name, password))
        );
        if (!password.equals(entity.getHashedPassword())) {
            sender.error("incorrect password passed");
            return false;
        }
        if (sessions.containsKey(name)) {
            sender.error("this player is already logged in");
            return false;
        }

        doLogin(name, password, initialPosition, sender);
        return true;
    }

    public synchronized void logout(String playerName) {
        if (!sessions.containsKey(playerName))
            return;

        log.info("logout({})", playerName);
        sessions.remove(playerName).getSender().disconnect();
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
                getPlayerFromEntity(repository.findByName(kv.getKey()).orElseThrow()),
                kv.getValue().getPosition()
            );
        for (Enemy enemy : getEnemies())
            observer.enemyAppears(enemy);
    }

    public synchronized void removeObserver(String id) {
        if (observers.remove(id) == null)
            throw new RuntimeException();
    }
}

package soturi.server;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import soturi.common.Registry;
import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.FightRecord;
import soturi.model.FightResult;
import soturi.model.ItemId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.PolygonWithDifficulty;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.model.messages_to_server.MessageToServerHandler;
import soturi.server.database.FightEntity;
import soturi.server.database.FightRepository;
import soturi.server.database.PlayerEntity;
import soturi.server.database.PlayerRepository;
import soturi.server.geo.CityProvider;
import soturi.server.geo.MonsterManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class GameService {
    private final PlayerRepository repository;
    private final FightRepository fightRepository;
    private final DynamicConfig dynamicConfig;
    private final CityProvider cityProvider;

    private Registry registry;
    private MonsterManager monsterManager;

    public GameService(PlayerRepository repository, FightRepository fightRepository, DynamicConfig dynamicConfig, CityProvider cityProvider) {
        this.repository = repository;
        this.fightRepository = fightRepository;
        this.dynamicConfig = dynamicConfig;
        this.cityProvider = cityProvider;
        registry = dynamicConfig.getRegistry();
        monsterManager = new MonsterManager(cityProvider, registry, this::nextEnemyId);
    }

    private final Map<String, PlayerSession> sessions = new LinkedHashMap<>();
    private final Map<String, MessageToClientHandler> observers = new LinkedHashMap<>();

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

    public synchronized void setConfig(Config config) {
        dynamicConfig.setConfigWithoutReloading(config);
        unregisterAllEnemies();
        registry = dynamicConfig.getRegistry();
        monsterManager = new MonsterManager(cityProvider, registry, this::nextEnemyId);

        for (PlayerSession session : sessions.values())
            session.getSender().setConfig(config);
        for (MessageToClientHandler obs : observers.values())
            obs.setConfig(config);
    }

    public synchronized List<Enemy> getEnemies() {
        return monsterManager.getAllEnemies();
    }

    public synchronized List<PolygonWithDifficulty> getAreas() {
        return monsterManager.getAreas();
    }

    public synchronized List<PlayerWithPosition> getPlayers() {
        return sessions.keySet().stream()
            .map(repository::findByName)
            .flatMap(Optional::stream)
            .map(this::getPlayerFromEntity)
            .map(p -> new PlayerWithPosition(p, sessions.get(p.name()).getPosition()))
            .toList();
    }

    public synchronized List<Player> getRegisteredPlayers() {
        return repository
            .findAll()
            .stream()
            .sorted(Comparator.comparingLong(PlayerEntity::getXp))
            .map(this::getPlayerFromEntity)
            .toList();
    }

    public synchronized void healPlayers() {
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            long missingHp = getPlayerFromEntity(entity).statistics().maxHp() - entity.getHp();
            long healed = (long) (missingHp * registry.getHealFraction()) + 1;
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

        int fxDelay = registry.getGiveFreeXpDelayInSeconds();
        if (fxDelay > 0 && secondCount % fxDelay == 0)
            giveFreeXp();

        int seDelay = registry.getSpawnEnemyDelayInSeconds();
        if (seDelay > 0 && secondCount % seDelay == 0)
            spawnEnemies();

        int hpDelay = registry.getHealDelayInSeconds();
        if (hpDelay > 0 && secondCount % hpDelay == 0)
            healPlayers();
    }

    @Scheduled(fixedDelay = 1000)
    private synchronized void tickEverySecond() {
        doTickEverySecond();
    }

    private synchronized void giveFreeXp() {
        log.info("giveFreeXp() called");

        long xp = registry.getGiveFreeXpAmount();
        if (xp == 0)
            return;
        for (String player : sessions.keySet()) {
            PlayerEntity entity = repository.findByName(player).orElseThrow();
            entity.addXp(xp);
            repository.save(entity);
            sendUpdatesFor(player);
        }
    }

    private long nextEnemyIdLong = 0;
    public EnemyId nextEnemyId() {
        return new EnemyId(nextEnemyIdLong++);
    }

    private synchronized void spawnEnemies() {
        Instant start = Instant.now();
        registerEnemies(monsterManager.generateEnemies());
        long mss = Duration.between(start, Instant.now()).toMillis();
        if (mss >= 5)
            log.info("spawnEnemies() took {}ms", mss);
    }

    public synchronized void registerEnemy(Enemy enemy) {
        registerEnemies(List.of(enemy));
    }

    public synchronized void registerEnemies(List<Enemy> enemies) {
        if (enemies.isEmpty())
            return;

        enemies.forEach(monsterManager::registerEnemy);

        for (var session : sessions.values())
            session.getSender().enemiesAppear(enemies);
        for (var sender : observers.values())
            sender.enemiesAppear(enemies);
    }

    private synchronized void unregisterEnemy(EnemyId enemyId) {
        unregisterEnemies(List.of(enemyId));
    }


    private synchronized void unregisterEnemies(List<EnemyId> enemyIds) {
        if (enemyIds.isEmpty())
            return;

        enemyIds.forEach(monsterManager::unregisterEnemy);

        for (var session : sessions.values())
            session.getSender().enemiesDisappear(enemyIds);
        for (var sender : observers.values())
            sender.enemiesDisappear(enemyIds);
    }

    public synchronized Player getPlayerFromEntity(PlayerEntity entity) {
        int lvl = registry.getLvlFromXp(entity.getXp());

        return new Player(
            entity.getName(),
            lvl,
            entity.getXp(),
            entity.getHp(),
            registry.getPlayerStatistics(lvl),
            entity.getEquipped().stream().map(ItemId::new).toList(),
            entity.getInventory().stream().map(ItemId::new).toList()
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

        if (playerData.hp() <= 0) {
            playerSession.getSender().error("You do not have any hp");
            return;
        }

        Enemy enemy = monsterManager.getEnemyMap().get(enemyId);
        if (enemy == null) {
            playerSession.getSender().error("this enemy does not exist");
            return;
        }
        if (enemy.position().distance(playerPosition) > registry.getFightingDistanceMaxInMeters()) {
            playerSession.getSender().error("this enemy is too far");
            return;
        }

        FightResult result = new FightSimulator(registry).simulateFight(playerData, enemy);
        playerSession.getSender().fightInfo(enemy.enemyId(), result);
        playerEntity.applyFightResult(result);
        repository.save(playerEntity);

        if (result.result() == Result.WON)
            unregisterEnemy(enemyId);

        FightRecord fightRecord = new FightRecord(
            new PlayerWithPosition(playerData, playerPosition),
            enemy,
            result,
            Instant.now()
        );

        fightRepository.save(new FightEntity(fightRecord));
        for (var sender : observers.values())
            sender.fightDashboardInfo(fightRecord);
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
                    PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
                    Player playerData = getPlayerFromEntity(playerEntity);

                    List<ItemId> equipped = new ArrayList<>(playerData.equipped());
                    List<ItemId> inventory = new ArrayList<>(playerData.inventory());

                    if (!inventory.remove(itemId)) {
                        sender.error("you do not have this item in inventory");
                        return;
                    }

                    for (ItemId otherItemId : playerData.equipped()) {
                        if (registry.getItemById(otherItemId).type() == registry.getItemById(itemId).type()) {
                            equipped.remove(otherItemId);
                            inventory.add(otherItemId);
                        }
                    }

                    equipped.add(itemId);
                    playerEntity.setEquipment(equipped, inventory);
                    repository.save(playerEntity);
                    sendUpdatesFor(playerName);
                }
            }

            @Override
            public void unequipItem(ItemId itemId) {
                synchronized (GameService.this) {
                    PlayerEntity playerEntity = repository.findByName(playerName).orElseThrow();
                    Player playerData = getPlayerFromEntity(playerEntity);

                    List<ItemId> equipped = new ArrayList<>(playerData.equipped());
                    List<ItemId> inventory = new ArrayList<>(playerData.inventory());

                    if (!equipped.remove(itemId)) {
                        sender.error("you do not have this item equipped");
                        return;
                    }

                    inventory.add(itemId);
                    playerEntity.setEquipment(equipped, inventory);
                    repository.save(playerEntity);
                    sendUpdatesFor(playerName);
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

    private synchronized void doLogin(@NonNull String name, @NonNull Position initialPosition,
                                      @NonNull MessageToClientHandler sender) {
        log.info("doLogin({})", name);
        sessions.put(name, new PlayerSession(sender, initialPosition, initialPosition));
        sender.setConfig(registry.getConfig());
        sendUpdatesFor(name);

        for (var kv : sessions.entrySet()) if (!kv.getKey().equals(name))
            sender.playerUpdate(
                getPlayerFromEntity(repository.findByName(kv.getKey()).orElseThrow()),
                kv.getValue().getPosition()
            );
        sender.enemiesAppear(getEnemies());
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
        if (!entity.hasPassword(password)) {
            sender.error("incorrect password passed");
            return false;
        }
        if (sessions.containsKey(name)) {
            sender.error("this player is already logged in");
            return false;
        }

        doLogin(name, initialPosition, sender);
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
        observer.enemiesAppear(getEnemies());
    }

    public synchronized void removeObserver(String id) {
        if (observers.remove(id) == null)
            throw new RuntimeException();
    }
}

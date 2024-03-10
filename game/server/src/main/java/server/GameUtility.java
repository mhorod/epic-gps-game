package server;

import lombok.extern.slf4j.Slf4j;
import model.Enemy;
import model.EnemyId;
import model.Player;
import model.PlayerWithPosition;
import model.Position;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

@Slf4j
@Component
public final class GameUtility {
    public final int giveFreeXpDelayInMilliseconds;
    public final long giveFreeXpAmount;
    public final int spawnEnemyDelayInMilliseconds;
    public final int spawnEnemyCapPerPlayer;
    public final int spawnEnemyMaxLvlDiff;
    public final int spawnEnemyMinDistToOthersInMeters;
    public final int spawnEnemyMaxDistToPlayersInMeters;
    public final int fightingMaxDistInMeters;

    public GameUtility(Environment env) {
        giveFreeXpDelayInMilliseconds = env.getRequiredProperty("give-free-xp.delay-in-milliseconds", int.class);
        giveFreeXpAmount = env.getRequiredProperty("give-free-xp.amount", long.class);
        spawnEnemyDelayInMilliseconds = env.getRequiredProperty("spawn-enemy.delay-in-milliseconds", int.class);
        spawnEnemyCapPerPlayer = env.getRequiredProperty("spawn-enemy.cap-per-player", int.class);
        spawnEnemyMaxLvlDiff = env.getRequiredProperty("spawn-enemy.max-lvl-diff", int.class);
        spawnEnemyMinDistToOthersInMeters = env.getRequiredProperty("spawn-enemy.min-dist-to-others-in-meters", int.class);
        spawnEnemyMaxDistToPlayersInMeters = env.getRequiredProperty("spawn-enemy.max-dist-to-players-in-meters", int.class);
        fightingMaxDistInMeters = env.getRequiredProperty("fighting.max-dist-in-meters", int.class);
    }

    /** Cumulative xp requirement for {@code lvl} (inclusive) */
    long getXpForLvlCumulative(int lvl) {
        if (lvl <= 1)
            return 0;
        return (long) (Math.pow(1.1, lvl - 2) * 100);
    }

    /** Xp requirement to for from {@code lvl} to {@code lvl + 1} */
    long getXpForNextLvl(int lvl) {
        if (lvl <= 1)
            return 0;
        return getXpForLvlCumulative(lvl + 1) - getXpForLvlCumulative(lvl);
    }

    int getLvlFromXp(long xp) {
        int lvl = 1;
        while (xp >= getXpForLvlCumulative(lvl + 1))
            ++lvl;
        return lvl;
    }

    Player getPlayerFromEntity(PlayerEntity entity) {
        int lvl = getLvlFromXp(entity.getXp());
        long maxHp = lvl * 5L;
        long attack = lvl * 3L;
        long defense = lvl * 2L;

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

    private long nextEnemyId = 0;
    private EnemyId generateEnemyId() {
        return new EnemyId(nextEnemyId++);
    }

    private final Random random = new Random();
    private Position generatePosition(Position around, double maxDistInMeters) {
        Function<Double, Double> generateDouble = r -> (random.nextDouble() - 0.5) * 2 * r;

        while (true) {
            // FIXME fix this
            double lat = around.latitude() + generateDouble.apply(maxDistInMeters / 1000000);
            double lng = around.longitude() + generateDouble.apply(maxDistInMeters / 1000000);
            Position generated = new Position(lat, lng);

            double distQuotient = around.distance(generated) / maxDistInMeters;
            log.debug("generatePosition() around: {}, generated: {}, distQuotient: {}",
                      around, generated, distQuotient);

            if (0.25 <= distQuotient && distQuotient <= 1)
                return generated;
        }
    }

    Optional<Enemy> generateOrdinaryEnemy(List<Enemy> enemies, List<PlayerWithPosition> players) {
        if (enemies.size() >= spawnEnemyCapPerPlayer * players.size())
            return Optional.empty();

        var center = players.get(random.nextInt(players.size()));
        Position enemyPosition = generatePosition(center.position(), spawnEnemyMaxDistToPlayersInMeters);

        for (var playerWithPosition : players)
            if (playerWithPosition.position().distance(enemyPosition) < spawnEnemyMinDistToOthersInMeters)
                return Optional.empty();
        for (var enemy : enemies)
            if (enemy.position().distance(enemyPosition) < spawnEnemyMinDistToOthersInMeters)
                return Optional.empty();

        Player centerPlayer = center.player();
        int enemyLvl = Math.max(1, random.nextInt(
                centerPlayer.lvl() - spawnEnemyMaxLvlDiff,
                centerPlayer.lvl() + spawnEnemyMaxLvlDiff + 1
        ));

        Enemy enemy = new Enemy(
            "Mrówka",
            enemyLvl,
            enemyPosition,
            generateEnemyId(),
            "gfx"
        );
        return Optional.of(enemy);
    }
}

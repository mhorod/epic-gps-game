package soturi.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

@Slf4j
@Component
public final class GameUtility {
    public final int giveFreeXpDelayInSeconds;
    public final long giveFreeXpAmount;
    public final int spawnEnemyDelayInSeconds;
    public final int spawnEnemyCapPerPlayer;
    public final int spawnEnemyMaxLvlDiff;
    public final int spawnEnemyMinDistToOthersInMeters;
    public final int spawnEnemyMaxDistToPlayersInMeters;
    public final int fightingMaxDistInMeters;

    public GameUtility(Environment env) {
        giveFreeXpDelayInSeconds = env.getRequiredProperty("give-free-xp.delay-in-seconds", int.class);
        giveFreeXpAmount = env.getRequiredProperty("give-free-xp.amount", long.class);
        spawnEnemyDelayInSeconds = env.getRequiredProperty("spawn-enemy.delay-in-seconds", int.class);
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
}

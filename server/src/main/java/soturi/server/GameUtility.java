package soturi.server;

import lombok.extern.slf4j.Slf4j;
import soturi.model.Player;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public final class GameUtility {
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

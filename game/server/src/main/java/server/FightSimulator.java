package server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.Enemy;
import model.FightResult;
import model.Player;
import model.Result;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
public final class FightSimulator {
    private final GameUtility gameUtility;

    @Getter
    @AllArgsConstructor
    private class Fighter {
        @Setter
        private long hp;

        private long attack, defense;
        private int lvl;

        void subHp(long hp) {
            long newHp = Math.max(0, Math.min(getHp(), getHp() - hp));
            setHp(newHp);
        }

        Fighter(Player player) {
            this(player.hp(), player.attack(), player.defense(), player.lvl());
        }
        Fighter(Enemy enemy) {
            this(enemy.lvl(), enemy.lvl(), enemy.lvl(), enemy.lvl());
        }
    }

    private void simulateFight(Fighter left, Fighter right) {
        long leftAttack = Math.max(left.getAttack() - right.getDefense(), 1);
        long rightAttack = Math.max(right.getAttack() - left.getDefense(), 1);

        while (left.getHp() > 0 && right.hp > 0) {
            right.subHp(leftAttack);
            if (right.getHp() == 0)
                break;
            left.subHp(rightAttack);
        }
    }

    private final Random random = new Random();
    public FightResult simulateFight(Player player, Enemy enemy) {
        Fighter playerFighter = new Fighter(player), enemyFighter = new Fighter(enemy);
        simulateFight(playerFighter, enemyFighter);

        long lostHp = player.hp() - playerFighter.getHp();
        if (playerFighter.getHp() == 0)
            return new FightResult(Result.LOST, lostHp, 0, List.of());

        return new FightResult(
            Result.WON,
            lostHp,
            (long) (gameUtility.getXpForNextLvl(player.lvl()) * random.nextDouble()),
            List.of()
        );
    }
}

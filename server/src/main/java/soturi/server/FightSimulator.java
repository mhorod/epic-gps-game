package soturi.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soturi.content.EnemyRegistry;
import soturi.model.Enemy;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.Result;
import org.springframework.stereotype.Component;
import soturi.model.messages_to_client.FightResult;

import java.util.Random;

@Component
@AllArgsConstructor
public final class FightSimulator {
    private final EnemyRegistry enemyRegistry = new EnemyRegistry(); // TODO get EnemyRegistry from somewhere else

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
            this(
                enemyRegistry.getEnemyHp(enemy),
                enemyRegistry.getEnemyAttack(enemy),
                enemyRegistry.getEnemyDefense(enemy),
                enemy.lvl()
            );
        }
    }

    private final Random random = new Random();
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

    public FightResult simulateFight(Player player, Enemy enemy) {
        Fighter playerFighter = new Fighter(player), enemyFighter = new Fighter(enemy);
        simulateFight(playerFighter, enemyFighter);

        long lostHp = player.hp() - playerFighter.getHp();
        Result result = Result.LOST;
        Loot loot = new Loot();

        if (playerFighter.getHp() > 0) {
            result = Result.WON;
            loot = enemyRegistry.lootFor(enemy);
        }

        return new FightResult(result, lostHp, enemy.enemyId(), loot);
    }
}

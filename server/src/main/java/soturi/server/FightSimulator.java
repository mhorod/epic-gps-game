package soturi.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soturi.common.Registry;
import soturi.model.Enemy;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.Result;
import soturi.model.messages_to_client.FightResult;

@AllArgsConstructor
public class FightSimulator {
    private final Registry registry;

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
            this(
                player.hp(),
                player.statistics().attack(),
                player.statistics().defense(),
                player.lvl()
            );
        }
        Fighter(Enemy enemy) {
            this(
                registry.getEnemyStatistics(enemy.lvl()).maxHp(),
                registry.getEnemyStatistics(enemy.lvl()).attack(),
                registry.getEnemyStatistics(enemy.lvl()).defense(),
                enemy.lvl()
            );
        }
    }

    private void simulateFightMutable(Fighter left, Fighter right) {
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
        simulateFightMutable(playerFighter, enemyFighter);

        long lostHp = player.hp() - playerFighter.getHp();
        Result result = Result.LOST;
        Loot loot = new Loot();

        if (playerFighter.getHp() > 0) {
            result = Result.WON;
            loot = registry.getLootFor(enemy);
        }

        return new FightResult(result, lostHp, enemy.enemyId(), loot);
    }
}

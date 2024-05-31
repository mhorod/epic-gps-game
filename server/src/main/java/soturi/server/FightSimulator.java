package soturi.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import soturi.common.Registry;
import soturi.model.Enemy;
import soturi.model.FightResult;
import soturi.model.Reward;
import soturi.model.Player;
import soturi.model.Result;

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

    private record FightResultInternal(Result attackerResult, long lostHpAttacker, long lostHpDefender) { }

    private FightResultInternal simulateFightInternal(Fighter attacker, Fighter defender) {
        long initHpAttacker = attacker.getHp();
        long initHpDefender = defender.getHp();

        simulateFightMutable(attacker, defender);

        long lostHpAttacker = initHpAttacker - attacker.getHp();
        long lostHpDefender = initHpDefender - defender.getHp();

        return new FightResultInternal(
            initHpAttacker == lostHpAttacker ? Result.LOST : Result.WON,
            lostHpAttacker,
            lostHpDefender
        );
    }

    public FightResult simulateFight(Player player, Enemy enemy) {
        FightResultInternal fri = simulateFightInternal(new Fighter(player), new Fighter(enemy));
        Reward reward = fri.attackerResult() == Result.WON ? registry.getRewardFor(enemy) : new Reward();

        return new FightResult(
            fri.attackerResult(),
            fri.lostHpAttacker(),
            reward
        );
    }
}

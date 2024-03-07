package server;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property = "type")
interface Message {
    record Position(double latitude, double longitude) {}
    // obrazki zembedowane w apce, mapping name->obrazek po stronie dominika
    record EnemyId(long id) {}
    record Enemy(String name, int lvl, EnemyId enemyId, Position position) {}
//    record Player(String name, int lvl) {}
    // broń, tarcza, hełm, zbroja, buty, rękawice
    enum ItemType {
        WEAPON, SHIELD, HELMET, ARMOR, BOOTS, GAUNTLETS
    }
    // czy Rękawice smoka wawelskiego mają konkretne staty
    // czy to typ, który może mieć różne staty
    record Item(String name, ItemType type, long hp, long attack, long defense) {}
    record Player(String name, int lvl, long xp, long hp, long maxHp, long attack, long defense, List<Item> equipped, List<Item> inventory) {}

    // apka -> serwer
    // przepisać mądrze
    record LoginInfo(String name, String password) implements Message {}

    record UpdateRealPosition(Position position) implements Message {}
    record UpdateLookingPosition(Position position) implements Message {}
    record AttackEnemy(EnemyId enemyId) implements Message {}
    record EquipItem(Item item) implements Message {}
    record UnequipItem(Item item) implements Message {}

    // serwer -> klient
    record MeUpdate(Player me) implements Message {}

    record FightResult(Result result, EnemyId enemyId) implements Message {
        enum Result {
            WON, LOST
        }
    }

    // te dwa także do dashboarda
    record EnemyAppears(Enemy enemy) implements Message {}
    record EnemyDisappears(EnemyId enemyId) implements Message {}
    record PlayerUpdate(Player player, Position position) {}
    record PlayerDisappears(String playerName) {}
}

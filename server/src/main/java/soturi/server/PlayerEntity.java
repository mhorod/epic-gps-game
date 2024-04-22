package soturi.server;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import soturi.model.FightResult;
import soturi.model.Item;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Entity
public final class PlayerEntity {
    @Getter @Id @GeneratedValue
    private Integer id;

    @Getter @Setter @Column(unique=true)
    private String name;

    @Getter @Setter
    private String hashedPassword;

    @Getter @Setter
    private long xp, hp;

    @Getter @Setter @ElementCollection
    private List<Integer> equipped, inventory;

    public PlayerEntity() {
        setHp(1);
        setEquipped(List.of());
        setInventory(List.of());
    }
    public PlayerEntity(String name, String hashedPassword) {
        this();
        setName(name);
        setHashedPassword(hashedPassword);
    }

    void addXp(long xp) {
        setXp(Math.max(0, xp) + getXp());
    }

    void addHp(long hp) {
        setHp(Math.max(0, getHp() + hp));
    }

    void applyFightResult(FightResult fightResult) {
        List<Integer> newInventory = Stream.concat(
                getInventory().stream(),
                fightResult.gainItems().stream().map(Item::itemId)
        ).toList();

        addHp(-fightResult.lostHp());
        addXp(fightResult.gainXp());
        setInventory(newInventory);
    }
}

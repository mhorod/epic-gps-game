package soturi.server;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import soturi.model.ItemId;
import soturi.model.messages_to_client.FightResult;

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

    @Getter @Setter @ElementCollection(fetch=FetchType.EAGER)
    private List<Long> equipped, inventory;

    public PlayerEntity() {
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

    void applyFightResult(FightResult fightDamage) {
        List<Long> newInventory = Stream.concat(
            getInventory().stream(),
            fightDamage.loot().items().stream().map(ItemId::id)
        ).toList();

        addHp(-fightDamage.lostHp());
        addXp(fightDamage.loot().xp());
        setInventory(newInventory);
    }
}

package soturi.server.database;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import soturi.model.FightResult;
import soturi.model.ItemId;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Entity
public class PlayerEntity {
    @Getter
    @Id
    @GeneratedValue
    private Integer id;

    @Getter
    @Setter
    @Column(unique = true)
    private String name;

    @Getter
    @Setter
    private String hashedPassword;

    @Getter
    @Setter
    private long xp, hp;

    @Getter
    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
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

    public boolean hasPassword(String password) {
        return hashPassword(password).equals(hashedPassword);
    }

    private static String hashPassword(String password) {
        // TODO: use real hashing
        return password;
    }
}

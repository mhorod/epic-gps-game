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
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.List;

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
    private String hashingAlgorithm;

    @Getter
    @Setter
    private long xp, hp;

    @Getter
    @Setter
    private UserRole role = UserRole.DEFAULT;

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
        if ("BCRYPT".equals(hashingAlgorithm))
            return BCrypt.checkpw(password, hashedPassword);
        else
            return hashedPassword.equals(password);
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public enum UserRole {
        DEFAULT,
        ADMIN
    }
}

package soturi.server.database;

import com.fasterxml.jackson.core.JacksonException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import soturi.JacksonConfig;
import soturi.model.FightRecord;

import java.util.Optional;

@Entity
public class FightEntity {
    @Getter @Id @GeneratedValue
    private Integer id;

    @Getter @Setter @Lob
    private String fight;

    public FightEntity() { }

    @SneakyThrows(JacksonException.class)
    public FightEntity(FightRecord record) {
        setFight(JacksonConfig.mapper.writeValueAsString(record));
    }

    public Optional<FightRecord> getFightRecord() {
        try {
            return Optional.of(JacksonConfig.mapper.readValue(getFight(), FightRecord.class));
        }
        catch (JacksonException ignored) {
            return Optional.empty();
        }
    }
}

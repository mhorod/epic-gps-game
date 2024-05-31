package soturi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import soturi.model.Config;
import soturi.model.EnemyId;
import soturi.model.messages_to_server.AttackEnemy;
import soturi.model.messages_to_server.MessageToServer;
import soturi.server.DynamicConfig;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class SerializationTests {
    ObjectMapper objectMapper = new JacksonConfig().objectMapper(new Jackson2ObjectMapperBuilder());

    @Test
    public void serializationCycle() throws Throwable {
        MessageToServer message = new AttackEnemy(new EnemyId(42));
        String serial = objectMapper.writeValueAsString(message);
        MessageToServer deserial = objectMapper.readValue(serial, MessageToServer.class);
        assertThat(message).isEqualTo(deserial);
    }

    @Test
    public void serializationCycle2() throws Throwable {
        Config cfg = new DynamicConfig(objectMapper, null).getDefaultConfig();
        String serial = objectMapper.writeValueAsString(cfg);
        Config deserial = objectMapper.readValue(serial, Config.class);
        assertThat(cfg).isEqualTo(deserial);
    }

    @Test
    public void serializationCycle3() throws Throwable {
        Duration dur = Duration.ofMinutes(5);
        String serial = objectMapper.writeValueAsString(dur);
        Duration deserial = objectMapper.readValue(serial, Duration.class);
        assertThat(dur).isEqualTo(deserial);
    }
}

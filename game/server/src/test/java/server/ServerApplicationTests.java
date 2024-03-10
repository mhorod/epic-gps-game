package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.EnemyId;
import model.messages_to_server.AttackEnemy;
import model.messages_to_server.MessageToServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServerApplicationTests {
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void serializationCycle() throws Throwable {
        MessageToServer message = new AttackEnemy(new EnemyId(42));
        String serial = objectMapper.writeValueAsString(message);
        MessageToServer deserial = objectMapper.readValue(serial, MessageToServer.class);
        assertThat(message).isEqualTo(deserial);
    }

    @Test
    public void serializationCycle2() throws Throwable {
        ObjectMapper normalMapper = new ObjectMapper();
        MessageToServer message = new AttackEnemy(new EnemyId(42));
        String serial = normalMapper.writeValueAsString(message);
        MessageToServer deserial = normalMapper.readValue(serial, MessageToServer.class);
        assertThat(message).isEqualTo(deserial);
    }
}

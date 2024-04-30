package gps.tracker;

import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import soturi.model.EnemyId;
import soturi.model.messages_to_server.AttackEnemy;
import soturi.model.messages_to_server.MessageToServer;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void serializationCycle() throws Throwable {
        ObjectMapper normalMapper = new ObjectMapper();
        MessageToServer message = new AttackEnemy(new EnemyId(42));
        String serial = normalMapper.writeValueAsString(message);
        MessageToServer deserial = normalMapper.readValue(serial, MessageToServer.class);
        assertEquals(message, deserial);
    }
}
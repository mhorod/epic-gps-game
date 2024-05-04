package soturi.server;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import soturi.server.communication.BlockingQueuePoll;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class BlockingQueuePollTest {
    @Test
    void pollTimeout() throws Throwable {
        BlockingQueue<Integer> bq = new LinkedBlockingQueue<>();
        bq.add(11);
        assertThat(BlockingQueuePoll.poll(bq, 1)).isEqualTo(11);
    }
}

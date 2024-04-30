package soturi.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_server.MessageToServer;
import soturi.model.messages_to_server.UpdateRealPosition;
import soturi.server.GameService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DashboardMockController {
    private final GameService gameService;
    private final ObjectMapper mapper;

    @PostMapping("/v1/mock/login")
    public boolean login(String name, String password) {
        return gameService.login(name, password, Position.KRAKOW, new MessageToClientFactory(m -> queueFor(name).add(m)));
    }

    @PostMapping("/v1/mock/logout")
    public void logout(String name) {
        gameService.logout(name);
    }

    Map<String, List<MessageToClient>> queues = Collections.synchronizedMap(new HashMap<>());

    private List<MessageToClient> queueFor(String name) {
        queues.computeIfAbsent(name, n -> Collections.synchronizedList(new ArrayList<>()));
        return queues.get(name);
    }

    @GetMapping("/v1/mock/queue")
    public List<MessageToClient> getQueue(String name) {
        List<MessageToClient> queue = queueFor(name);
        synchronized (queue) {
            List<MessageToClient> ret = new ArrayList<>(queue);
            queue.clear();
            return ret;
        }
    }

    @PostMapping("/v1/mock/player/{name}/updateRealPosition")
    public void updateRealPosition(@PathVariable String name, double latitude, double longitude) {
        new UpdateRealPosition(new Position(latitude, longitude)).process(gameService.receiveFrom(name));
    }

    @SneakyThrows
    @PostMapping("/v1/mock/send")
    public void send(String name, String msg) {
        MessageToServer message = mapper.readValue(msg, MessageToServer.class);
        message.process(gameService.receiveFrom(name));
    }
}

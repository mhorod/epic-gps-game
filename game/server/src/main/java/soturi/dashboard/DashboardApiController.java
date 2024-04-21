package soturi.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.model.Area;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.model.RectangularArea;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_server.MessageToServer;
import soturi.server.GameService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DashboardApiController {

    private final GameService gameService;
    private final ObjectMapper mapper;

    @GetMapping("/v1/enemies")
    public List<Enemy> getEnemies() {
        List<Enemy> enemies = gameService.getEnemies();
        Enemy enemy = new Enemy(
                "Pomidor",
                4,
                new Position(50, 27),
                new EnemyId(123),
                "gfx"
        );
        if (enemies.isEmpty())
            return List.of(enemy);
        else
            return enemies;
    }

    @GetMapping("/v1/players")
    public List<PlayerWithPosition> getPlayers() {
       List<PlayerWithPosition> players = gameService.getPlayers();
       Player player = new Player(
               "Student TCS", 5,
               100, 100, 100, 3, 4, List.of(), List.of()
       );
       if (players.isEmpty())
           return List.of(new PlayerWithPosition(player, new Position(49, 27)));
       else
           return players;
    }

    @GetMapping("/v1/areas")
    public List<Area> getAreas() {
        RectangularArea area = new RectangularArea(Position.KRAKOW, Position.WARSZAWA);
        var l1 = area.quadSplit();
        var l2 = l1.get(0).quadSplit();
        var l3 = l1.get(2).quadSplit();

        List<Area> ret = new ArrayList<>();
        ret.add(new Area(l1.get(1), 3));
        ret.add(new Area(l1.get(3), 4));
        for (var v : l2)
            ret.add(new Area(v, 1));
        for (var v : l3)
            ret.add(new Area(v, 2));
        return ret;
    }

    Map<String, List<MessageToClient>> queues = Collections.synchronizedMap(new HashMap<>());
    private List<MessageToClient> queueFor(String name) {
        if (!queues.containsKey(name))
            queues.put(name, Collections.synchronizedList(new ArrayList<>()));
        return queues.get(name);
    }

    @GetMapping("/v1/mock/login")
    public boolean login(String name, String password) {
        return gameService.login(name, password, new MessageToClientFactory(m -> queueFor(name).add(m)));
    }

    @GetMapping("/v1/mock/logout")
    public void logout(String name) {
        gameService.logout(name);
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

    @SneakyThrows
    @GetMapping("/v1/mock/send")
    public void send(String name, String msg) {
        MessageToServer message = mapper.readValue(msg, MessageToServer.class);
        message.process(gameService.sendTo(name));
    }
}

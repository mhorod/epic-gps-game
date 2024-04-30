package soturi.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.model.Area;
import soturi.server.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.model.messages_to_server.MessageToServer;
import soturi.server.GameService;
import soturi.server.geo.MonsterManager;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardApiController {
    private final MonsterManager monsterManager;
    private final GameService gameService;
    private final Config config;
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
        return monsterManager.getAreas();
    }

    @PostMapping("/v1/config")
    public void changeConfig(String key, String value) {
        config.setValue(key, value);
    }

    @PostMapping("/v1/reload")
    public void reload() {
        gameService.reload();
    }
}

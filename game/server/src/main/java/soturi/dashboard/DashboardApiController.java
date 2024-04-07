package soturi.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.server.GameService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardApiController {

    private final GameService gameService;

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
}

package soturi.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.model.Area;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Position;
import soturi.model.RectangularArea;
import soturi.server.GameService;

import java.util.ArrayList;
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
}

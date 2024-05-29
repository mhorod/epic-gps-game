package soturi.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import soturi.JacksonConfig;
import soturi.common.Registry;
import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyType;
import soturi.model.FightRecord;
import soturi.model.Player;
import soturi.model.PlayerWithPosition;
import soturi.model.Polygon;
import soturi.model.PolygonId;
import soturi.model.PolygonWithDifficulty;
import soturi.model.Position;
import soturi.model.Statistics;
import soturi.server.DynamicConfig;
import soturi.server.GameService;
import soturi.server.database.FightEntity;
import soturi.server.database.FightRepository;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DashboardApiController {
    private final GameService gameService;
    private final DynamicConfig dynamicConfig;
    private final ObjectMapper mapper;
    private final FightRepository fightRepository;

    @GetMapping("/v1/enemies")
    public List<Enemy> getEnemies() {
        return gameService.getEnemies();
    }

    @GetMapping("/v1/players")
    public List<PlayerWithPosition> getPlayers() {
        List<PlayerWithPosition> players = gameService.getPlayers();
        Player player = new Player(
            "Student TCS", 5,
            100, 100, new Statistics(500, 5, 5), List.of(), List.of()
        );
        if (players.isEmpty())
            return List.of(new PlayerWithPosition(player, new Position(49, 27)));
        else
            return players;
    }

    @GetMapping("/v1/registered-players")
    public List<Player> getRegisteredPlayers() {
        return gameService.getRegisteredPlayers();
    }

    @GetMapping("/v1/areas")
    public List<PolygonWithDifficulty> getAreas() {
        return gameService.getAreas();
    }

    @GetMapping("/v1/wkt-areas")
    public String getAreasAsWKT() {
        List<Polygon> polys = gameService.getAreas().stream().map(PolygonWithDifficulty::polygon).toList();
        return Polygon.asWKT(polys);
    }

    @GetMapping("/v1/polygon-wkt-converter")
    public String getPolygonAsWKT(String name) {
        Polygon poly = dynamicConfig.getRegistry().getPolygonById(new PolygonId(name));
        if (poly == null)
            throw new RuntimeException("polygon %s not found".formatted(name));
        return poly.asWKT();
    }

    @PostMapping("/v1/config-kv")
    public void changeConfig(String key, String value) throws Exception {
        Config config = dynamicConfig.getRegistry().getConfig();
        Method setter = Arrays.stream(Config.class.getMethods())
            .filter(m -> m.getName().equalsIgnoreCase("with" + key)).findFirst().orElseThrow();

        Object obj = mapper.readValue(value, setter.getParameterTypes()[0]);
        Config newConfig = (Config) setter.invoke(config, obj);
        setConfig(newConfig);
    }

    @PostMapping("/v1/reload-config")
    public void reloadConfigFile() {
        Config config = dynamicConfig.tryToLoad().orElseThrow();
        setConfig(config);
    }

    @PostMapping("/v1/set-config")
    public void setConfig(@RequestBody Config config) {
        gameService.setConfig(config);
        dynamicConfig.tryToDump(config);
    }

    @GetMapping("/v1/config")
    public Config getConfig() {
        return dynamicConfig.getRegistry().getConfig();
    }

    @GetMapping("/v1/fight-list")
    public List<FightRecord> getFightList(int limit) throws  Exception {
        System.out.println(mapper.writeValueAsString(Instant.now()));
        return fightRepository
            .findAllByOrderByIdDesc(PageRequest.of(0, limit))
            .stream()
            .map(FightEntity::getFightRecord)
            .flatMap(Optional::stream)
            .toList();
    }

    @PostMapping("/v1/kill-all-enemies")
    public void killAllEnemies() {
        gameService.unregisterAllEnemies();
    }

    @GetMapping("/v1/info/xp")
    public String xpInfo() { // TODO quick and dirty, to rewrite
        Registry registry = dynamicConfig.getRegistry();

        StringBuilder sb = new StringBuilder();
        sb.append("Current lvl | Xp for next lvl | Xp gain | Xp gain (%) | Enemies to lvl up\n");
        for (int i = 1; i < registry.getMaxLvl(); ++i) {
            long xpForNext = registry.getXpForNextLvl(i);
            EnemyType type = registry.getEnemyTypesPerLvl(i)
                .stream()
                .filter(x -> x.xpFactor() == 1)
                .findFirst()
                .orElseThrow();
            long xpGain = registry.getLootFor(new Enemy(type.typeId(), new EnemyId(0), i, Position.KRAKOW)).xp();
            double gainRel = 1.0 * xpGain / xpForNext;
            double toBeat = 1.0 / gainRel;
            sb.append("%d | %d | %d | %.3f | %.3f\n".formatted(i, xpForNext, xpGain, gainRel, toBeat));
        }

        return sb.toString();
    }

    @GetMapping("/v1/current-user")
    public String currentUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

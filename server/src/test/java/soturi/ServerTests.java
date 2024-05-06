package soturi;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import soturi.content.GeoRegistry;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyTypeId;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.RectangularArea;
import soturi.model.Result;
import soturi.model.messages_to_client.Disconnect;
import soturi.model.messages_to_client.EnemyDisappears;
import soturi.model.messages_to_client.Error;
import soturi.model.messages_to_client.FightResult;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.server.Config;
import soturi.server.GameService;
import soturi.server.PlayerRepository;
import soturi.server.geo.CityProvider;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:application.yml", properties="spring.datasource.url=jdbc:h2:mem:")
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class ServerTests {
    @Autowired
    GameService gameService;
    @Autowired
    PlayerRepository repository;
    @Autowired
    CityProvider cityProvider;
    @Autowired
    Config config;

    @BeforeAll
    void setupGameService() {
        config.v.giveFreeXpDelayInSeconds = 0;
        config.v.spawnEnemyDelayInSeconds = 0;
        config.v.fightingMaxDistInMeters = 50;
        config.v.geoMaxSplit = 2;
        config.v.setGeoFromArea(GeoRegistry.POLAND);
        gameService.reloadDynamicConfig();
    }

    @BeforeEach
    void cleanGameService() {
        gameService.kickAllObservers();
        gameService.kickAllPlayers();
        gameService.unregisterAllEnemies();
        repository.deleteAll();
    }

    private Enemy newEnemy(int lvl, Position position, EnemyId enemyId) {
        return new Enemy(
            new EnemyTypeId(0),
            enemyId,
            lvl,
            position,
            "name",
            "name"
        );
    }

    @Test
    void null_player_name() {
        assertThat(gameService.login(null, "password", GeoRegistry.KRAKOW, mock())).isFalse();
    }
    @Test
    void empty_player_name() {
        assertThat(gameService.login("", "password", GeoRegistry.KRAKOW, mock())).isFalse();
    }
    @Test
    void null_password() {
        assertThat(gameService.login("user", null, GeoRegistry.KRAKOW, mock())).isFalse();
    }
    @Test
    void player_registration() {
        assertThat(gameService.login("user", "pass", GeoRegistry.KRAKOW, mock())).isTrue();
    }
    @Test
    void player_login_logout_cycle() {
        assertThat(gameService.login("user", "pass", GeoRegistry.KRAKOW, mock())).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "pass", GeoRegistry.KRAKOW, mock())).isTrue();
    }
    @Test
    void player_enters_incorrect_password() {
        assertThat(gameService.login("user", "pass", GeoRegistry.KRAKOW, mock())).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "----", GeoRegistry.KRAKOW, mock())).isFalse();
    }
    @Test
    void observers_get_notified_about_player_joining() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        assertThat(gameService.login("name", "password", GeoRegistry.KRAKOW, mock())).isTrue();
        Player player = gameService.getPlayers().getFirst().player();

        verify(observer).playerUpdate(eq(player), any());
    }
    @Test
    void observers_get_notified_about_player_leaving() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        gameService.login("name", "password", GeoRegistry.KRAKOW, mock());
        gameService.logout("name");

        verify(observer).playerDisappears("name");
    }
    @Test
    void player_attacks_non_existent_monster() {
        List<MessageToClient> received = new ArrayList<>();
        gameService.login("p", "", GeoRegistry.KRAKOW, new MessageToClientFactory(received::add));
        gameService.receiveFrom("p").attackEnemy(new EnemyId(0));

        assertThat(received)
            .anyMatch(Error.class::isInstance)
            .noneMatch(Disconnect.class::isInstance)
            .noneMatch(FightResult.class::isInstance)
            .noneMatch(EnemyDisappears.class::isInstance);
    }
    @Test
    void player_attacks_enemy_too_far_away() {
        List<MessageToClient> received = new ArrayList<>();
        gameService.login("p", "", GeoRegistry.KRAKOW, new MessageToClientFactory(received::add));

        Enemy enemy = newEnemy(1, GeoRegistry.WARSZAWA, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());

        assertThat(received)
            .anyMatch(Error.class::isInstance)
            .noneMatch(Disconnect.class::isInstance)
            .noneMatch(FightResult.class::isInstance)
            .noneMatch(EnemyDisappears.class::isInstance);
        assertThat(gameService.getEnemies()).containsExactly(enemy);
    }
    @Test
    void player_attacks_monster_and_wins() {
        MessageToClientHandler received = mock();
        gameService.login("p", "", GeoRegistry.KRAKOW, received);

        Enemy enemy = newEnemy(1, GeoRegistry.KRAKOW, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(eq(Result.WON), anyLong(), eq(enemy.enemyId()), any());
        verify(received).enemyDisappears(enemy.enemyId());
        verify(received, never()).disconnect();
    }
    @Test
    void player_attacks_monster_and_loses() {
        MessageToClientHandler received = mock();
        gameService.login("p", "", GeoRegistry.KRAKOW, received);

        Enemy enemy = newEnemy(100, GeoRegistry.KRAKOW, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(eq(Result.LOST), anyLong(), eq(enemy.enemyId()), eq(new Loot()));
        verify(received, never()).enemyDisappears(enemy.enemyId());
        verify(received, never()).disconnect();
    }
    @Test
    void wieliczka_to_wieś() {
        assertThat(cityProvider.getCities())
            .anyMatch(c -> c.name().equals("Wieliczka") && c.population() < 50000);
    }
}

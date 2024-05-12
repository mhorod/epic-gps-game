package soturi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import soturi.common.Registry;
import soturi.model.Config;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.EnemyType;
import soturi.model.Loot;
import soturi.model.Player;
import soturi.model.PolygonId;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.Disconnect;
import soturi.model.messages_to_client.EnemiesDisappear;
import soturi.model.messages_to_client.Error;
import soturi.model.messages_to_client.FightResult;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.MessageToClientHandler;
import soturi.server.DynamicConfig;
import soturi.server.FightSimulator;
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
public class ServerTests {
    @Autowired
    GameService gameService;
    @Autowired
    PlayerRepository repository;
    @Autowired
    CityProvider cityProvider;
    @Autowired
    DynamicConfig dynamicConfig;
    Registry registry;
    FightSimulator fightSimulator;
    PolygonId POLAND = new PolygonId("POLAND");

    @BeforeEach
    void cleanGameService() {
        Config defaultConfig = dynamicConfig.getDefaultConfig();
        Config testConfig = defaultConfig
            .withGiveFreeXpDelayInSeconds(0)
            .withSpawnEnemyDelayInSeconds(0)
            .withHealDelayInSeconds(0)
            .withGameAreaId(POLAND);

        gameService.setConfig(testConfig);
        registry = dynamicConfig.getRegistry();
        fightSimulator = new FightSimulator(registry);

        gameService.kickAllObservers();
        gameService.kickAllPlayers();
        gameService.unregisterAllEnemies();
        repository.deleteAll();
    }

    private Enemy newEnemy(int lvl, Position position, EnemyId enemyId) {
        EnemyType type = registry
            .getEnemyTypesPerLvl(lvl)
            .stream()
            .filter(t -> t.xpFactor() == 1)
            .findFirst()
            .orElseThrow();
        return new Enemy(type.typeId(), enemyId, lvl, position);
    }

    private void healPlayers() {
        for (int i = 0; i < 100; ++i)
            gameService.healPlayers();
    }

    @Test
    void null_player_name() {
        assertThat(gameService.login(null, "password", Position.KRAKOW, mock())).isFalse();
    }
    @Test
    void empty_player_name() {
        assertThat(gameService.login("", "password", Position.KRAKOW, mock())).isFalse();
    }
    @Test
    void null_password() {
        assertThat(gameService.login("user", null, Position.KRAKOW, mock())).isFalse();
    }
    @Test
    void player_registration() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock())).isTrue();
    }
    @Test
    void player_login_logout_cycle() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock())).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock())).isTrue();
    }
    @Test
    void player_enters_incorrect_password() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock())).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "----", Position.KRAKOW, mock())).isFalse();
    }
    @Test
    void observers_get_notified_about_player_joining() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        assertThat(gameService.login("name", "password", Position.KRAKOW, mock())).isTrue();
        Player player = gameService.getPlayers().getFirst().player();

        verify(observer).playerUpdate(eq(player), any());
    }
    @Test
    void observers_get_notified_about_player_leaving() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        gameService.login("name", "password", Position.KRAKOW, mock());
        gameService.logout("name");

        verify(observer).playerDisappears("name");
    }
    @Test
    void player_attacks_non_existent_monster() {
        List<MessageToClient> received = new ArrayList<>();
        gameService.login("p", "", Position.KRAKOW, new MessageToClientFactory(received::add));
        gameService.receiveFrom("p").attackEnemy(new EnemyId(0));
        healPlayers();

        assertThat(received)
            .anyMatch(Error.class::isInstance)
            .noneMatch(Disconnect.class::isInstance)
            .noneMatch(FightResult.class::isInstance)
            .noneMatch(EnemiesDisappear.class::isInstance);
    }
    @Test
    void player_attacks_enemy_too_far_away() {
        List<MessageToClient> received = new ArrayList<>();
        gameService.login("p", "", Position.KRAKOW, new MessageToClientFactory(received::add));

        Enemy enemy = newEnemy(1, Position.WARSZAWA, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());

        assertThat(received)
            .anyMatch(Error.class::isInstance)
            .noneMatch(Disconnect.class::isInstance)
            .noneMatch(FightResult.class::isInstance)
            .noneMatch(EnemiesDisappear.class::isInstance);
        assertThat(gameService.getEnemies()).containsExactly(enemy);
    }
    @Test
    void player_attacks_monster_and_wins() {
        MessageToClientHandler received = mock();
        gameService.login("p", "", Position.KRAKOW, received);
        healPlayers();

        Enemy enemy = newEnemy(1, Position.KRAKOW, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(eq(Result.WON), anyLong(), eq(enemy.enemyId()), any());
        verify(received).enemiesDisappear(List.of(enemy.enemyId()));
        verify(received, never()).disconnect();
    }
    @Test
    void player_attacks_monster_and_loses() {
        MessageToClientHandler received = mock();
        gameService.login("p", "", Position.KRAKOW, received);
        healPlayers();

        Enemy enemy = newEnemy(100, Position.KRAKOW, new EnemyId(0));
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(eq(Result.LOST), anyLong(), eq(enemy.enemyId()), eq(new Loot()));
        verify(received, never()).enemiesDisappear(any());
        verify(received, never()).disconnect();
    }
    @Test
    void wieliczka_to_wieś() {
        assertThat(cityProvider.getCities())
            .anyMatch(c -> c.name().equals("Wieliczka") && c.population() < 50000);
    }
    @Test
    void lvl_1_player_stats_make_sense() {
        gameService.login("p", "", Position.KRAKOW, mock());
        Player beforeHeal = gameService.getPlayers().get(0).player();
        healPlayers();
        Player afterHeal = gameService.getPlayers().get(0).player();

        assertThat(beforeHeal.hp()).isZero();
        assertThat(afterHeal.hp()).isPositive().isGreaterThan((long) (beforeHeal.statistics().maxHp() * 0.85));

        assertThat(beforeHeal.statistics().maxHp()).isEqualTo(afterHeal.statistics().maxHp()).isPositive();
        assertThat(beforeHeal.statistics().attack()).isEqualTo(afterHeal.statistics().attack()).isPositive();
        assertThat(beforeHeal.statistics().defense()).isEqualTo(afterHeal.statistics().defense()).isPositive();
    }
    @Test
    void lvl_1_player_beats_lvl_1_enemy_and_receives_damage() {
        gameService.login("p", "", Position.KRAKOW, mock());
        healPlayers();

        Player p = gameService.getPlayers().get(0).player();
        Enemy e = newEnemy(1, Position.KRAKOW, new EnemyId(0));

        FightResult result = fightSimulator.simulateFight(p, e);

        assertThat(result.result()).isEqualTo(Result.WON);
        assertThat(result.lostHp()).isPositive().isGreaterThan((long) (p.statistics().maxHp() * 0.15));
    }
    @Test
    void lvl_1_enemy_xp_loot_makes_sense() {
        Enemy e = newEnemy(1, Position.KRAKOW, new EnemyId(0));
        long xp = registry.getLootFor(e).xp();

        long xp_to_2 = registry.getXpForLvlCumulative(2);
        long xp_to_3 = registry.getXpForLvlCumulative(3);

        assertThat(xp).isPositive()
            .isGreaterThan((long) (xp_to_2 * 0.33))
            .isLessThan(xp_to_3);
    }
    @Test
    void lvl_10_enemy_xp_loot_makes_sense() {
        Enemy e = newEnemy(1, Position.KRAKOW, new EnemyId(0));
        long xp = registry.getLootFor(e).xp();

        long xp_to_11 = registry.getXpForNextLvl(10);

        System.err.println(xp);
        System.err.println(xp_to_11);

        assertThat(xp).isPositive()
            .isGreaterThan((long) (xp_to_11 * 0.05))
            .isLessThan((long) (xp_to_11 * 0.25));
    }
    @Test
    void xp_requirement_make_sense() {
        assertThat(registry.getXpForNextLvl(1)).isGreaterThanOrEqualTo(75);

        for (int i = 1; i < 100; ++i)
            assertThat(registry.getXpForNextLvl(i))
                .as("Xp requirement for lvl %d", i)
                .isGreaterThan(registry.getXpForNextLvl(i-1));
    }
    @Test
    void there_is_enemy_for_each_lvl() {
        for (int i = 1; i < 100; ++i)
            assertThat(newEnemy(i, Position.KRAKOW, new EnemyId(0))).isNotNull();
    }
}

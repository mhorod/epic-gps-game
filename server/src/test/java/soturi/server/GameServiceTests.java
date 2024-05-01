package soturi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import soturi.model.Enemy;
import soturi.model.EnemyId;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.Result;
import soturi.model.messages_to_client.Disconnect;
import soturi.model.messages_to_client.EnemyDisappears;
import soturi.model.messages_to_client.Error;
import soturi.model.messages_to_client.FightResult;
import soturi.model.messages_to_client.MessageToClient;
import soturi.model.messages_to_client.MessageToClientFactory;
import soturi.model.messages_to_client.MessageToClientHandler;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:application.yml", properties="spring.datasource.url=jdbc:h2:mem:")
@SpringBootTest
public class GameServiceTests {
    @Autowired
    GameService gameService;
    @Autowired
    PlayerRepository repository;
    @Autowired
    Config config;

    @BeforeEach
    void setupGameService() {
        config.v.giveFreeXpDelayInSeconds = 0;
        config.v.spawnEnemyDelayInSeconds = 0;
        gameService.stopAndDisconnectAll();
        repository.deleteAll();
    }

    @Test
    void null_player_name() {
        assertThat(gameService.login(null, "password", Position.KRAKOW, mock(MessageToClientHandler.class))).isFalse();
    }
    @Test
    void empty_player_name() {
        assertThat(gameService.login("", "password", Position.KRAKOW, mock(MessageToClientHandler.class))).isFalse();
    }
    @Test
    void null_password() {
        assertThat(gameService.login("user", null, Position.KRAKOW, mock(MessageToClientHandler.class))).isFalse();
    }
    @Test
    void player_registration() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock(MessageToClientHandler.class))).isTrue();
    }
    @Test
    void player_login_logout_cycle() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock(MessageToClientHandler.class))).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock(MessageToClientHandler.class))).isTrue();
    }
    @Test
    void player_enters_incorrect_password() {
        assertThat(gameService.login("user", "pass", Position.KRAKOW, mock(MessageToClientHandler.class))).isTrue();
        gameService.logout("user");
        assertThat(gameService.login("user", "----", Position.KRAKOW, mock(MessageToClientHandler.class))).isFalse();
    }
    @Test
    void observers_get_notified_about_player_joining() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        assertThat(gameService.login("name", "password", Position.KRAKOW, mock(MessageToClientHandler.class))).isTrue();
        Player player = gameService.getPlayers().getFirst().player();

        verify(observer).playerUpdate(eq(player), any());
    }
    @Test
    void observers_get_notified_about_player_leaving() {
        MessageToClientHandler observer = mock(MessageToClientHandler.class);

        gameService.addObserver("o1", observer);
        gameService.login("name", "password", Position.KRAKOW, mock(MessageToClientHandler.class));
        gameService.logout("name");

        verify(observer).playerDisappears("name");
    }
    @Test
    void player_attacks_non_existent_monster() {
        List<MessageToClient> received = new ArrayList<>();
        gameService.login("p", "", Position.KRAKOW, new MessageToClientFactory(received::add));
        gameService.receiveFrom("p").attackEnemy(new EnemyId(0));

        assertThat(received)
            .anyMatch(Error.class::isInstance)
            .noneMatch(Disconnect.class::isInstance)
            .noneMatch(FightResult.class::isInstance)
            .noneMatch(EnemyDisappears.class::isInstance);
    }
    @Test
    void player_attacks_monster_and_wins() {
        MessageToClientHandler received = mock(MessageToClientHandler.class);
        gameService.login("p", "", Position.KRAKOW, received);

        Enemy enemy = new Enemy("a", 1, Position.KRAKOW, new EnemyId(0), "g");
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(Result.WON, enemy.enemyId());
        verify(received).enemyDisappears(enemy.enemyId());
    }
    @Test
    void player_attacks_monster_and_loses() {
        MessageToClientHandler received = mock(MessageToClientHandler.class);
        gameService.login("p", "", Position.KRAKOW, received);

        Enemy enemy = new Enemy("a", 100, Position.KRAKOW, new EnemyId(0), "g");
        gameService.registerEnemy(enemy);

        gameService.receiveFrom("p").attackEnemy(enemy.enemyId());
        verify(received).fightResult(Result.LOST, enemy.enemyId());
        verify(received, never()).enemyDisappears(enemy.enemyId());
    }
}

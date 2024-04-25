package soturi.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import soturi.model.Player;
import soturi.model.Position;
import soturi.model.messages_to_client.MessageToClientHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@TestPropertySource(locations = "classpath:application.properties", properties="spring.datasource.url=jdbc:h2:mem:")
@SpringBootTest
public class GameServiceTests {
    @Autowired
    GameService gameService;
    @Autowired
    PlayerRepository repository;

    @BeforeEach
    void setupGameService() {
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
}

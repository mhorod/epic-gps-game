package soturi.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FreeXpService {
    private final PlayerService playerService;

    FreeXpService(PlayerService playerService) {
        this.playerService = playerService;
        new Thread(this::work).start();
    }

    @SneakyThrows
    private void work() {
        while (true) {
            log.info("free xp for {} players", playerService.getAllLoggedIn().size());
            for (String player : playerService.getAllLoggedIn())
                playerService.gainXp(player, 3);
            Thread.sleep(1000 * 10);
        }
    }
}

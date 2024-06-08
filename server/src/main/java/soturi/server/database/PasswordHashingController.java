package soturi.server.database;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PasswordHashingController {
    private final PlayerRepository playerRepository;



    @RolesAllowed("ADMIN")
    @PostMapping("/v1/hash-passwords")
    void hashPasswords() {
        List<PlayerEntity> players = playerRepository.findAll();
        for (PlayerEntity p : players) {
            if ("BCRYPT2".equals(p.getHashingAlgorithm())) continue;
            String current = p.getHashedPassword();
            p.setHashedPassword(PlayerEntity.hashPassword(current));
            p.setHashingAlgorithm("BCRYPT");
        }

        playerRepository.saveAll(players);

    }
}

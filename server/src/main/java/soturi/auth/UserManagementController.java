package soturi.auth;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.server.database.PlayerEntity.UserRole;
import soturi.server.database.PlayerRepository;

@RestController
@RequiredArgsConstructor
public class UserManagementController {
    private final PlayerRepository playerRepository;

    @RolesAllowed("ADMIN")
    @PostMapping("/v1/set-role")
    public void setRole(String userName, UserRole role) {
        playerRepository.findByName(userName)
                .map(p -> {
                    p.setRole(role);
                    return p;
                })
                .ifPresent(playerRepository::save);
    }
}

package soturi.web;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import soturi.server.GameService;
import soturi.server.database.FightRepository;
import soturi.server.database.PlayerEntity;
import soturi.server.database.PlayerEntity.UserRole;
import soturi.server.database.PlayerRepository;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SoturiWebController {
    private final GameService gameService;
    private final PlayerRepository playerRepository;
    private final FightRepository fightRepository;

    // Rendering is done in React which also handles these paths
    @RolesAllowed("ADMIN")
    @GetMapping(value = {"/dashboard", "/dashboard/map-view", "/dashboard/spawn-areas", "/dashboard/players", "/dashboard/enemies", "/dashboard/fight-history"})
    public String dashboardIndex() {
        return "dashboard/index";
    }

    @GetMapping("/")
    public String index(Model model) {
        String username = Optional.ofNullable(
                        SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElse(null);
        String role = playerRepository.findByName(username).map(PlayerEntity::getRole)
                .orElse(UserRole.DEFAULT).toString();
        model.addAttribute("username", username);
        model.addAttribute("players", playerRepository.count());
        model.addAttribute("fights", fightRepository.count());
        model.addAttribute("enemies", gameService.getEnemyCount());
        model.addAttribute("role", role);
        return "website/index";
    }

    @GetMapping("/log-in")
    public String getLogIn(Model model, @RequestParam Optional<String> redirect) {
        redirect.ifPresent(value -> model.addAttribute("redirect", value));
        return "website/log-in";
    }

    @GetMapping("/logout")
    public String logOut() {
        return "website/log-out";
    }

    @GetMapping("/logged-out")
    public String loggedOut() {
        return "website/logged-out";
    }


    @GetMapping("/sign-up")
    public String signUp() {
        return "website/sign-up";
    }

}

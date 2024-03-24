package soturi.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import soturi.model.Position;

import java.util.List;

@Slf4j
@Controller
public class DashboardController {

    private record EnemyDto(String name, int lvl, Position position) { }

    private record PlayerDto(String name, int lvl, Position position) { }

    @GetMapping("/")
    String index(Model model) {
        EnemyDto enemy = new EnemyDto(
                "Żaba",
                3,
                new Position(50.06783, 19.90868)
        );
        model.addAttribute("enemies", List.of(enemy));


        PlayerDto player = new PlayerDto(
                "Student TCS",
                5,
                new Position(50.06883, 19.91868)
        );
        model.addAttribute("players", List.of(player));

        return "index";
    }
}

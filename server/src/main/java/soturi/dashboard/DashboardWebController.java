package soturi.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import soturi.server.GameService;


@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardWebController {
    // Rendering is done in React which also handles these paths
    @GetMapping(value = {"/", "map-view", "spawn-areas", "players", "enemies"})
    public String index() {
        return "index";
    }
}

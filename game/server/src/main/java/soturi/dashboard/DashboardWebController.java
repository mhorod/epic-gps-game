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



    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/spawn-areas")
    public String spawnAreas() {
        return "spawn-areas";
    }

    @GetMapping("/map-view")
    public String mapView() {
        return "map-view";
    }
}

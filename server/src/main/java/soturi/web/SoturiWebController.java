package soturi.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SoturiWebController {

    // Rendering is done in React which also handles these paths
    @GetMapping(value = {"/dashboard", "/dashboard/map-view", "/dashboard/spawn-areas", "/dashboard/players", "/dashboard/enemies"})
    public String dashboardIndex() {
        return "dashboard/index";
    }

    @GetMapping("/")
    public String index() {
        return "website/index";
    }
}

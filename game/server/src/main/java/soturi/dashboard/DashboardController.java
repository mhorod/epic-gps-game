package soturi.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class DashboardController {

    @GetMapping("/")
    String index() {
        return "index";
    }
}

package soturi.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SoturiWebController {

    // Rendering is done in React which also handles these paths
    @GetMapping(value = {"/dashboard", "/dashboard/map-view", "/dashboard/spawn-areas", "/dashboard/players", "/dashboard/enemies", "/dashboard/fight-history"})
    public String dashboardIndex() {
        return "dashboard/index";
    }

    @GetMapping("/")
    public String index() {
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

package soturi.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import soturi.server.database.PlayerEntity;
import soturi.server.database.PlayerRepository;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final PlayerRepository playerRepository;

    @PostMapping(path = "/log-in", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RedirectView processLogIn(
            @ModelAttribute LogInForm form,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        Optional<PlayerEntity> entity = playerRepository
                .findByName(form.username)
                .filter(player -> player.hasPassword(form.password()));
        if (entity.isEmpty()) {
            redirectAttributes.addAttribute("redirect", form.redirect);
            return new RedirectView("/log-in");
        }

        String token = jwtService.createToken(form.username());

        Cookie cookie = new Cookie("token", token);
        response.addCookie(cookie);
        String url = form.redirect != null ? form.redirect : "/";
        return new RedirectView(url);
    }


    public record LogInForm(String username, String password, String redirect) { }
}

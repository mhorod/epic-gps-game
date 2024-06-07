package soturi.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import soturi.server.database.PlayerEntity;
import soturi.server.database.PlayerEntity.UserRole;
import soturi.server.database.PlayerRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final PlayerRepository playerRepository;


    @EventListener(ApplicationReadyEvent.class)
    public void grantAdmin() {
        playerRepository.findByName("hihal2").map(
                p -> {
                    p.setRole(UserRole.ADMIN);
                    return p;
                }
        ).ifPresent(playerRepository::save);
        log.info("hihal2 is admin now");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getContextPath().equals("/logout")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getTokenFromCookie(request);
            if (token != null && jwtService.isTokenValid(token)) {
                String username = jwtService.getUsername(token);
                PlayerEntity user = playerRepository.findByName(username).orElse(null);
                if (user == null)
                    return;

                List<GrantedAuthority> authorities = new ArrayList<>();
                if (UserRole.ADMIN.equals(user.getRole()))
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities);
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .map(header -> header.substring(7))
                .orElse(null);
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        return Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .map(cookies -> cookies.filter(
                        cookie -> cookie.getName().equals("token")))
                .flatMap(Stream::findFirst)
                .map(Cookie::getValue)
                .orElse(null);
    }
}

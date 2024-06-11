package soturi.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import soturi.server.database.PlayerRepository;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableWebSocketSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
class WebSecurityConfiguration {

    private static final String[] PUBLIC_URLS = {
            "/", "/static/**", "/log-in", "/logout", "/logged-out", "/sign-in", "/error", "/ws/game", "/health-check", "/post-log"
    };

    @Bean
    public SecurityFilterChain filterChain(AuthTokenFilter authTokenFilter, HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> {
                                           Arrays.stream(PUBLIC_URLS)
                                                   .forEach(url -> auth.requestMatchers(url).permitAll());
                                           auth.requestMatchers("/swagger-ui/**").hasRole("ADMIN");
                                           auth.anyRequest().authenticated();
                                       }
                ).logout(logout -> logout.logoutSuccessUrl("/logged-out").deleteCookies("token"))
                .anonymous(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/post-log")
                );
        http.addFilterAfter(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(
            JwtService jwtService,
            PlayerRepository playerRepository
    ) {
        return new AuthTokenFilter(jwtService, playerRepository);
    }

    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages
    ) {
        return messages.nullDestMatcher().authenticated()
                .build();
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}

package soturi.server;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import soturi.dashboard.DashboardWebSocketController;

@Slf4j
@AllArgsConstructor
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketGameController webSocketGameController;
    private final DashboardWebSocketController dashboardWebSocketController;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketGameController, "/ws/game").setAllowedOrigins("*");
        registry.addHandler(dashboardWebSocketController, "/ws/dashboard").setAllowedOrigins("*");
    }
}

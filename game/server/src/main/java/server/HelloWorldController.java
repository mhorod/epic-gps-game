package server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class HelloWorldController {
    @GetMapping("/v1/hello-world")
    HelloWorldResponse helloWorld() {
        return new HelloWorldResponse("Hello, world!");
    }

    List<String> current = new ArrayList<>();

    @GetMapping("/v1/get-list")
    ListResponse getList() {
        return new ListResponse(current);
    }

    @PostMapping("/v1/push-list")
    void getList(@RequestParam String str) {
        current.add(str);
    }

    record HelloWorldResponse(String message) {}

    record ListResponse(List<String> list) {}
}

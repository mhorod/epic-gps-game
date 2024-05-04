package soturi;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ServerApplication {
    @SneakyThrows
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Runtime.getRuntime().halt(0)));
        SpringApplication.run(ServerApplication.class, args);
    }
}

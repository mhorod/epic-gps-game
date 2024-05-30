package soturi.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.common.VersionInfo;

import java.time.LocalDateTime;

@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    public HealthCheckDto getHealthCheck() {
        return new HealthCheckDto(
            VersionInfo.compilationTime, VersionInfo.commitId
        );
    }

    public record HealthCheckDto(LocalDateTime compilationTime, String commit) {

    }
}

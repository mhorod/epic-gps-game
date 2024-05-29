package soturi.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import soturi.common.CompilationInfo;

import java.time.LocalDateTime;

@RestController
public class HealthCheckController {

    @GetMapping("/health-check")
    public HealthCheckDto getHealthCheck() {
        return new HealthCheckDto(
                CompilationInfo.getCompilationTime().orElse(null),
                CompilationInfo.getCommitId().orElse(null)
        );
    }

    public record HealthCheckDto(LocalDateTime compilationTime, String commit) {

    }
}

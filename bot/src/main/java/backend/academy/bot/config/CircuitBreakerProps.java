package backend.academy.bot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.http.circuit-breaker")
public record CircuitBreakerProps(
    @Min(1) int slidingWindowSize,
    @Min(1) int minimumRequiredCalls,
    @Min(1) int failureRateThreshold,
    @Min(1) int permittedCallsInHalfOpenState,
    @NotNull Duration waitDurationInOpenState) { }


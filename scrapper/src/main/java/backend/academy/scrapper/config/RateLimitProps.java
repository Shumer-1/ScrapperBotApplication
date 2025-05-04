package backend.academy.scrapper.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProps(
    @Min(1) int capacity,
    @Min(1) int refillTokens,
    @NotNull Duration period,
    @Min(100) int errorCode,
    @NotEmpty String errorMessage) { }


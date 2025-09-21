package backend.academy.scrapper.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.http.retry")
public record RetryProps(
    @NotNull @Min(1) Integer attempts,
    @NotNull Duration backoff,
    @NotEmpty List<Integer> statusCodes) { }


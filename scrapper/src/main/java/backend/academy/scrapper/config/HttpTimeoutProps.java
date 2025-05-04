package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.http")
public record HttpTimeoutProps(
    @NotNull Duration connectTimeout,
    @NotNull Duration readTimeout,
    @NotNull Duration writeTimeout,
    @NotNull Duration responseTimeout) { }


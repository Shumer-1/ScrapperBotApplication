package backend.academy.bot.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.http")
public record HttpTimeoutProps(
    @NotNull Duration connectTimeout,
    @NotNull Duration readTimeout,
    @NotNull Duration writeTimeout,
    @NotNull Duration responseTimeout) { }


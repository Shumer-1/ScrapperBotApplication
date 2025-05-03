package backend.academy.scrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
public record ScrapperConfig(@NotEmpty String githubToken, StackOverflowCredentials stackOverflow) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

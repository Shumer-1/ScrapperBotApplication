package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GitHubClient {

    private final WebClient webClient;
    private final String githubToken;

    public GitHubClient(WebClient webClient, ScrapperConfig config) {
        this.webClient = webClient;
        this.githubToken = config.githubToken();
    }

    public Mono<String> getRepositoryInfo(String repoApiUrl) {
        return webClient
                .get()
                .uri(convertToApiUrl(repoApiUrl))
                .header("Authorization", "token " + githubToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<Instant> getLastUpdateTime(String repoApiUrl) {
        return webClient
                .get()
                .uri(convertToApiUrl(repoApiUrl))
                .header("Authorization", "token " + githubToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    String pushedAt = json.get("pushed_at").asText();
                    return Instant.parse(pushedAt);
                });
    }

    private String convertToApiUrl(String url) {
        if (url.contains("github.com") && !url.contains("api.github.com")) {
            return url.replace("https://github.com", "https://api.github.com/repos");
        }
        return url;
    }
}

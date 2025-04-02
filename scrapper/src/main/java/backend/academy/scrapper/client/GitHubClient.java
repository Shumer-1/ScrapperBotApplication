package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.model.dto.GitHubIssue;
import backend.academy.scrapper.service.sourceHandlers.GitHubSourceHandler;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GitHubClient {
    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);
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

    public Mono<GitHubIssue> getLatestIssueOrPR(String repoUrl) {
        log.info("GitHubClient смотрит последние изменения от {}", repoUrl);
        String issuesApiUrl = convertToApiUrl(repoUrl) + "/issues?state=open&sort=created&direction=desc&per_page=1";
        return webClient
                .get()
                .uri(issuesApiUrl)
                .header("Authorization", "token " + githubToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnNext(json -> log.debug("Получен JSON: {}", json))
                .flatMap(json -> {
                    if (json.isArray() && !json.isEmpty()) {
                        JsonNode latest = json.get(0);
                        String title = latest.get("title").asText();
                        String username = latest.get("user").get("login").asText();
                        Instant createdAt =
                                Instant.parse(latest.get("created_at").asText());
                        String body =
                                latest.hasNonNull("body") ? latest.get("body").asText() : "";
                        String bodyPreview = body.length() > 200 ? body.substring(0, 200) : body;
                        GitHubIssue issue = new GitHubIssue(title, username, createdAt, bodyPreview);
                        log.info("Получили информацию от GitHub: {}, {}, {}", title, username, createdAt);
                        return Mono.just(issue);
                    }
                    return Mono.empty();
                });
    }

    private String convertToApiUrl(String url) {
        if (url.contains("github.com") && !url.contains("api.github.com")) {
            return url.replace("https://github.com", "https://api.github.com/repos");
        }
        return url;
    }
}

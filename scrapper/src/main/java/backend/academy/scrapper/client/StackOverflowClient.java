package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.ScrapperConfig.StackOverflowCredentials;
import backend.academy.scrapper.model.dto.StackOverflowUpdate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StackOverflowClient {

    private final WebClient webClient;
    private final String key;
    private final ObjectMapper mapper = new ObjectMapper();

    public StackOverflowClient(WebClient webClient, ScrapperConfig config) {
        this.webClient = webClient;
        StackOverflowCredentials creds = config.stackOverflow();
        this.key = creds.key();
    }

    public Mono<String> getQuestionInfo(String questionId) {
        String url = String.format(
            "https://api.stackexchange.com/2.3/questions/%s?order=desc&sort=activity&site=stackoverflow&key=%s",
            questionId, key);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class);
    }

    public Mono<StackOverflowUpdate> getLatestAnswer(String questionId) {
        String answersUrl = String.format(
            "https://api.stackexchange.com/2.3/questions/%s/answers?order=desc&sort=creation&site=stackoverflow&filter=withbody&key=%s&pagesize=1",
            questionId, key);
        return getLatest(questionId, answersUrl);
    }

    public Mono<StackOverflowUpdate> getLatestComment(String questionId) {
        String commentsUrl = String.format(
            "https://api.stackexchange.com/2.3/questions/%s/comments?order=desc&sort=creation&site=stackoverflow&filter=withbody&key=%s&pagesize=1",
            questionId, key);
        return getLatest(questionId, commentsUrl);
    }

    private Mono<StackOverflowUpdate> getLatest(String questionId, String url) {
        return webClient.get().uri(url)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .flatMap(json -> processLatest(json, questionId));
    }

    private Mono<StackOverflowUpdate> processLatest(JsonNode json, String questionId) {
        JsonNode items = json.get("items");
        if (items != null && items.isArray() && !items.isEmpty()) {
            JsonNode latest = items.get(0);
            long creationEpoch = latest.get("creation_date").asLong();
            Instant creationTime = Instant.ofEpochSecond(creationEpoch);
            String body = latest.hasNonNull("body") ? latest.get("body").asText() : "";
            String preview = body.length() > 200 ? body.substring(0, 200) : body;
            String author = latest.get("owner").get("display_name").asText();
            return getQuestionInfo(questionId).flatMap(info -> {
                try {
                    JsonNode infoJson = mapper.readTree(info);
                    JsonNode infoItems = infoJson.get("items");
                    String title = "";
                    if (infoItems != null && infoItems.isArray() && infoItems.size() > 0) {
                        title = infoItems.get(0).get("title").asText();
                    }
                    StackOverflowUpdate update =
                        new StackOverflowUpdate(title, author, creationTime, preview);
                    return Mono.just(update);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
        }
        return Mono.empty();
    }
}

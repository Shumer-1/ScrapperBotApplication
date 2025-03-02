package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.ScrapperConfig.StackOverflowCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StackOverflowClient {

    private final WebClient webClient;
    private final String key;

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

    public Mono<Instant> getQuestionLastActivity(String questionId) {
        String url = String.format(
                "https://api.stackexchange.com/2.3/questions/%s?order=desc&sort=activity&site=stackoverflow&key=%s",
                questionId, key);
        return webClient.get().uri(url).retrieve().bodyToMono(JsonNode.class).map(json -> {
            JsonNode items = json.get("items");
            if (items != null && items.isArray() && !items.isEmpty()) {
                long timestamp = items.get(0).get("last_activity_date").asLong();
                return Instant.ofEpochSecond(timestamp);
            }
            return Instant.EPOCH;
        });
    }
}

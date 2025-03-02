package backend.academy.scrapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import backend.academy.scrapper.client.StackOverflowClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class StackOverflowClientTests {

    private WebClient webClient;
    private StackOverflowClient stackOverflowClient;
    private ScrapperConfig scrapperConfig;
    private ObjectMapper objectMapper = new ObjectMapper();

    private WebClient.RequestHeadersUriSpec<?> uriSpec;
    private WebClient.RequestHeadersSpec<?> headersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        webClient = mock(WebClient.class);
        scrapperConfig = mock(ScrapperConfig.class);
        ScrapperConfig.StackOverflowCredentials creds = mock(ScrapperConfig.StackOverflowCredentials.class);
        doReturn("dummy-key").when(creds).key();
        doReturn(creds).when(scrapperConfig).stackOverflow();

        uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        headersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();

        stackOverflowClient = new StackOverflowClient(webClient, scrapperConfig);
    }

    @Test
    public void testGetQuestionInfo_HttpError() {
        WebClientResponseException exception =
                WebClientResponseException.create(404, "Not Found", null, null, StandardCharsets.UTF_8);
        doReturn(Mono.error(exception)).when(responseSpec).bodyToMono(String.class);

        Mono<String> result = stackOverflowClient.getQuestionInfo("123456");

        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof WebClientResponseException
                        && ((WebClientResponseException) err).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    public void testGetQuestionLastActivity_InvalidBody() throws Exception {
        String jsonString = "{\"wrong_field\": \"value\"}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        doReturn(Mono.just(jsonNode)).when(responseSpec).bodyToMono(JsonNode.class);

        Mono<Instant> result = stackOverflowClient.getQuestionLastActivity("123456");

        StepVerifier.create(result).expectNext(Instant.EPOCH).verifyComplete();
    }

    @Test
    public void testGetQuestionLastActivity_ValidResponse() throws Exception {
        String jsonString = "{\"items\": [{\"last_activity_date\": 1730390400}]}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        doReturn(Mono.just(jsonNode)).when(responseSpec).bodyToMono(JsonNode.class);

        Mono<Instant> result = stackOverflowClient.getQuestionLastActivity("123456");

        StepVerifier.create(result)
                .expectNext(Instant.ofEpochSecond(1730390400))
                .verifyComplete();
    }
}

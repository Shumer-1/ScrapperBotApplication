package backend.academy.scrapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.GitHubClient;
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

public class GitHubClientTest {

    private WebClient webClient;
    private GitHubClient gitHubClient;
    private ScrapperConfig scrapperConfig;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        webClient = mock(WebClient.class);
        scrapperConfig = mock(ScrapperConfig.class);
        when(scrapperConfig.githubToken()).thenReturn("dummy-token");
        gitHubClient = new GitHubClient(webClient, scrapperConfig);
    }

    @Test
    public void testGetRepositoryInfoHttpError() {
        WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();

        WebClientResponseException exception =
                WebClientResponseException.create(400, "Bad Request", null, null, StandardCharsets.UTF_8);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(exception));

        Mono<String> result = gitHubClient.getRepositoryInfo("https://github.com/user/repo");

        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof WebClientResponseException
                        && ((WebClientResponseException) err).getStatusCode().value() == 400)
                .verify();
    }

    @Test
    public void testGetLastUpdateTimeInvalidBody() throws Exception {
        WebClient.RequestHeadersUriSpec<?> uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        doReturn(uriSpec).when(webClient).get();
        doReturn(headersSpec).when(uriSpec).uri(anyString());
        doReturn(headersSpec).when(headersSpec).header(anyString(), anyString());
        doReturn(responseSpec).when(headersSpec).retrieve();

        String jsonString = "{\"wrong_field\": \"2024-11-14T12:34:54Z\"}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(jsonNode));

        Mono<Instant> result = gitHubClient.getLastUpdateTime("https://github.com/user/repo");

        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof NullPointerException)
                .verify();
    }
}

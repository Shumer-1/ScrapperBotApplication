package backend.academy.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import backend.academy.bot.config.CircuitBreakerProps;
import backend.academy.bot.config.HttpTimeoutProps;
import backend.academy.bot.config.RetryProps;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(
    classes = {
        BotConfig.class,
        CircuitBreakerAutoConfiguration.class,  // если используете авто-конфиг Resilience4j
        HttpTimeoutProps.class,
        RetryProps.class,
        CircuitBreakerProps.class
    },
    properties = {
        "app.telegramToken=dummy",
    }
)
@AutoConfigureWireMock(port = 0)
class CircuitBreakerTest {

    private static final String PATH = "/delayed";

    @Autowired
    private WebClient webClient;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private CircuitBreakerRegistry cbRegistry;

    private CircuitBreaker cb;

    @BeforeEach
    void setUp() {
        cb = cbRegistry.circuitBreaker("backendServiceCb");
        cb.getEventPublisher().onStateTransition(evt -> {  });
        cb.transitionToOpenState();
    }

    @Test
    void whenCircuitIsOpen_thenRequestShortCircuitsBeforeHttpTimeout() {
        stubFor(get(urlEqualTo(PATH))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(5_000)));

        long start = System.currentTimeMillis();

        CallNotPermittedException ex = assertThrows(CallNotPermittedException.class, () ->
            webClient.get()
                .uri("http://localhost:{port}" + PATH, wireMockServer.port())
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10))
        );

        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 1_000,
            "CircuitBreaker должен коротить запрос сразу, а не ждать 5-секундного ответа; вместо " + elapsed + " мс");

        assertEquals(CircuitBreaker.State.OPEN, cb.getState());
    }
}


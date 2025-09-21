package backend.academy.scrapper;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.TransportConfig;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяем, что после первой неудачи Circuit-Breaker открывается
 * и второй вызов завершается CallNotPermittedException быстрее сетевых таймаутов.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {          // фиктивные токены → контекст собирается без real-секретов
    "GITHUB_TOKEN=dummy",
    "SO_TOKEN_KEY=dummy",
    "SO_ACCESS_TOKEN=dummy"
})
@ExtendWith(WireMockExtension.class)
class CircuitBreakerFailFastTest {

    /* ---------- тестовая инфраструктура ------------------------------------------------------ */

    private String baseUrl;
    private WebClient webClient;               // WebClient c нужными таймаутами
    private final CircuitBreaker cb =          // CB, который открывается после ПЕРВОЙ ошибки
        CircuitBreaker.of("slowApi", CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(1)
            .minimumNumberOfCalls(1)
            .failureRateThreshold(1)           // 1 неудача ⇒ 100 % ошибок ⇒ OPEN
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(1)
            .recordException(e -> true)        // любая ошибка — причина фейла
            .build());

    @BeforeEach
    void setUpStubAndClient(WireMockRuntimeInfo wm) {
        if (baseUrl == null) {                         // регистрируем WireMock только один раз
            baseUrl = wm.getHttpBaseUrl();             // http://localhost:{port}
            wm.getWireMock().register(
                get(urlPathEqualTo("/slow"))
                    .willReturn(aResponse()
                        .withFixedDelay(15_000)   // дольше responseTimeout-а (10 с)
                        .withStatus(504))
            );
        }

        /*   WebClient со «строгими» таймаутами: 5 с connect-timeout + 10 с response-timeout   */
        HttpClient httpClient = HttpClient.create()
            .option(TransportConfig.CONNECT_TIMEOUT_MILLIS, 5_000)
            .responseTimeout(Duration.ofSeconds(10));

        webClient = WebClient.builder()
            .clientConnector(new reactor.netty.http.client.HttpClientConnector(httpClient))
            .build();
    }

    /* ---------- полезный метод --------------------------------------------------------------- */

    private Mono<Void> callSlow() {
        return webClient.get()
            .uri(baseUrl + "/slow")
            .retrieve()
            .bodyToMono(Void.class)
            .transformDeferred(CircuitBreakerOperator.of(cb));   // вешаем CB поверх вызова
    }

    /* ---------- сам тест --------------------------------------------------------------------- */

    @Test
    void circuitOpensAfterFirstTimeout_andFailsFastOnSecondCall() {

        /* 1) первый запрос → зависает дольше responseTimeout-а и падает ReadTimeoutException */
        StepVerifier.create(callSlow())
            .expectError()                                    // любая ошибка с таймаута
            .verify(Duration.ofSeconds(12));                  // 10 с + небольшой запас

        /* 2) CB уже OPEN → CallNotPermittedException приходит «мгновенно» */
        long start = System.nanoTime();

        StepVerifier.create(callSlow())
            .expectError(CallNotPermittedException.class)
            .verify(Duration.ofMillis(300));                  // надёжный запас

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(tookMs)
            .as("Ошибка должна прийти быстрее connect-timeout’а (5 000 мс)")
            .isLessThan(500);
    }
}

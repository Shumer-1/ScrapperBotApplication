package backend.academy.bot;

import backend.academy.bot.config.CircuitBreakerProps;
import backend.academy.bot.config.HttpTimeoutProps;
import backend.academy.bot.config.RetryProps;
import com.pengrad.telegrambot.TelegramBot;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
@EnableAsync
@ConfigurationPropertiesScan
public record BotConfig(@NotEmpty String telegramToken) implements AsyncConfigurer {
    @Bean
    public TelegramBot telegramBot(BotConfig botConfig) {
        return new TelegramBot(botConfig.telegramToken());
    }
    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerProps props) {
        CircuitBreakerConfig cfg = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(props.slidingWindowSize())
            .minimumNumberOfCalls(props.minimumRequiredCalls())
            .failureRateThreshold(props.failureRateThreshold())
            .permittedNumberOfCallsInHalfOpenState(props.permittedCallsInHalfOpenState())
            .waitDurationInOpenState(props.waitDurationInOpenState())
            .recordExceptions(Throwable.class)
            .build();

        return CircuitBreaker.of("backendServiceCb", cfg);
    }


    @Bean
    public WebClient webClient(HttpTimeoutProps t, RetryProps retryProps, CircuitBreakerProps circuitBreakerProps) {
        HttpClient http = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) t.connectTimeout().toMillis())
            .responseTimeout(t.responseTimeout())
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(
                    t.readTimeout().toMillis(),  TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(
                    t.writeTimeout().toMillis(), TimeUnit.MILLISECONDS)));


        ExchangeFilterFunction cbFilter = (request, next) ->
            next.exchange(request)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker(circuitBreakerProps)));

        ExchangeFilterFunction retryFilter = (request, next) -> next.exchange(request)
            .flatMap(response -> {
                if (shouldRetry(request, response, retryProps)) {
                    return response.createException()
                        .flatMap(Mono::error);
                }
                return Mono.just(response);
            })
            .retryWhen(Retry.fixedDelay(retryProps.attempts(), retryProps.backoff())
                .filter(ex -> isRetryable(ex, retryProps)));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(http))
            .filter(cbFilter)
            .filter(retryFilter)
            .build();
    }
    private boolean shouldRetry(ClientRequest req, ClientResponse resp, RetryProps rp) {
        boolean codeMatch = rp.statusCodes().contains(resp.statusCode().value());
        boolean methodOk  = req.method() != null &&
            List.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.PUT,
                    HttpMethod.DELETE, HttpMethod.OPTIONS)
                .contains(req.method());
        return codeMatch && methodOk;
    }

    private boolean isRetryable(Throwable ex, RetryProps rp) {
        return ex instanceof WebClientResponseException wce &&
            rp.statusCodes().contains(wce.getRawStatusCode());
    }

    @Bean(name = "botTaskExecutor")
    public Executor botTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AsyncNotifier-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return botTaskExecutor();
    }
}

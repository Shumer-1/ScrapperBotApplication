package backend.academy.bot;

import com.pengrad.telegrambot.TelegramBot;
import jakarta.validation.constraints.NotEmpty;
import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@EnableAsync
public record BotConfig(@NotEmpty String telegramToken) implements AsyncConfigurer {
    @Bean
    public TelegramBot telegramBot(BotConfig botConfig) {
        return new TelegramBot(botConfig.telegramToken());
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
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

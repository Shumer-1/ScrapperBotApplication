package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.entity.Link;
import backend.academy.bot.model.entity.User;
import backend.academy.bot.service.LinkCacheService;
import com.redis.testcontainers.RedisContainer;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
@Testcontainers
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LinkCacheServiceTest {

    private static final long USER_ID = 42L;

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:7.2.4");

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        ScrapperClient scrapperClient() {
            return Mockito.mock(ScrapperClient.class);
        }

        @Bean
        @Primary
        KafkaTemplate<Object, Object> kafkaTemplate() {
            return Mockito.mock(KafkaTemplate.class);
        }

        @Bean
        @Primary
        ConsumerFactory<Object, Object> consumerFactory() {
            return Mockito.mock(ConsumerFactory.class);
        }
    }

    @Autowired
    private LinkCacheService linkCacheService;

    @Autowired
    private ScrapperClient scrapperClient;

    private final List<Link> dummyLinks = List.of(
            new Link("https://foo.bar/1", new User(123L, "123", List.of()), Set.of(), Set.of()),
            new Link("https://foo.bar/2", new User(123L, "123", List.of()), Set.of(), Set.of()));

    @BeforeEach
    void setUp() {
        linkCacheService.invalidateCache(USER_ID).block();
        Mockito.reset(scrapperClient);
    }

    @Test
    void shouldFetchLinksAndCacheThem() {
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID)).thenReturn(Mono.just(dummyLinks));

        StepVerifier.create(linkCacheService.getLinks(USER_ID))
                .assertNext(list -> assertThat(list)
                        .extracting(Link::getLink)
                        .containsExactly("https://foo.bar/1", "https://foo.bar/2"))
                .verifyComplete();

        Mockito.verify(scrapperClient, times(1)).getLinksByUserId(USER_ID);
    }

    @Test
    void shouldReturnCachedLinksOnSubsequentCalls() {
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID)).thenReturn(Mono.just(dummyLinks));

        linkCacheService.getLinks(USER_ID).block();

        StepVerifier.create(linkCacheService.getLinks(USER_ID))
                .assertNext(list -> assertThat(list)
                        .extracting(Link::getLink)
                        .containsExactly("https://foo.bar/1", "https://foo.bar/2"))
                .verifyComplete();

        Mockito.verify(scrapperClient, times(1)).getLinksByUserId(USER_ID);
    }

    @Test
    void shouldInvalidateEmptyCache() {
        StepVerifier.create(linkCacheService.invalidateCache(USER_ID))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldFetchLinksAfterInvalidation() {
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID)).thenReturn(Mono.just(dummyLinks));
        linkCacheService.getLinks(USER_ID).block();
        Mockito.verify(scrapperClient, times(1)).getLinksByUserId(USER_ID);

        StepVerifier.create(linkCacheService.invalidateCache(USER_ID))
                .expectNext(true)
                .verifyComplete();

        Mockito.when(scrapperClient.getLinksByUserId(USER_ID)).thenReturn(Mono.just(dummyLinks));
        StepVerifier.create(linkCacheService.getLinks(USER_ID))
                .assertNext(list -> assertThat(list)
                        .extracting(Link::getLink)
                        .containsExactly("https://foo.bar/1", "https://foo.bar/2"))
                .verifyComplete();

        Mockito.verify(scrapperClient, times(2)).getLinksByUserId(USER_ID);
    }
}

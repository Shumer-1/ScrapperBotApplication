package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.entities.Link;
import backend.academy.bot.model.entities.User;
import backend.academy.bot.services.LinkCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LinkCacheServiceTest {

    private static final long USER_ID = 42L;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6.2.6")
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        ScrapperClient scrapperClient() {
            return Mockito.mock(ScrapperClient.class);
        }
    }

    @Autowired
    private LinkCacheService linkCacheService;

    @Autowired
    private ScrapperClient scrapperClient;

    private final List<Link> dummyLinks = List.of(
        new Link("https://foo.bar/1", new User(123L, "123", List.of()), Set.of(), Set.of()),
        new Link("https://foo.bar/2", new User(123L, "123", List.of()), Set.of(), Set.of())
    );

    @BeforeEach
    void setUp() {
        linkCacheService.invalidateCache(USER_ID).block();
        Mockito.reset(scrapperClient);
    }

    @Test
    void shouldFetchLinksAndCacheThem() {
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID))
            .thenReturn(Mono.just(dummyLinks));

        StepVerifier.create(linkCacheService.getLinks(USER_ID))
            .assertNext(list -> assertThat(list)
                .extracting(Link::getLink)
                .containsExactly("https://foo.bar/1", "https://foo.bar/2"))
            .verifyComplete();

        Mockito.verify(scrapperClient, times(1)).getLinksByUserId(USER_ID);
    }

    @Test
    void shouldReturnCachedLinksOnSubsequentCalls() {
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID))
            .thenReturn(Mono.just(dummyLinks));

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
        Mockito.when(scrapperClient.getLinksByUserId(USER_ID))
            .thenReturn(Mono.just(dummyLinks));
        linkCacheService.getLinks(USER_ID).block();
        Mockito.verify(scrapperClient, times(1)).getLinksByUserId(USER_ID);

        StepVerifier.create(linkCacheService.invalidateCache(USER_ID))
            .expectNext(true)
            .verifyComplete();

        Mockito.when(scrapperClient.getLinksByUserId(USER_ID))
            .thenReturn(Mono.just(dummyLinks));

        StepVerifier.create(linkCacheService.getLinks(USER_ID))
            .assertNext(list -> assertThat(list)
                .extracting(Link::getLink)
                .containsExactly("https://foo.bar/1", "https://foo.bar/2"))
            .verifyComplete();

        Mockito.verify(scrapperClient, times(2)).getLinksByUserId(USER_ID);
    }
}

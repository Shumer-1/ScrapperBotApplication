package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.entity.Link;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LinkCacheService {
    private static final String KEY_PREFIX = "links:";

    private static final Duration TTL = Duration.ofMinutes(5);
    private final ReactiveRedisOperations<String, Link> redisOps;
    private final ScrapperClient scrapperClient;

    public LinkCacheService(ReactiveRedisOperations<String, Link> redisOps, ScrapperClient scrapperClient) {
        this.redisOps = redisOps;
        this.scrapperClient = scrapperClient;
    }

    public Mono<Boolean> invalidateCache(Long userId) {
        String key = KEY_PREFIX + userId;
        return redisOps.delete(key).hasElement();
    }

    public Mono<List<Link>> getLinks(Long userId) {
        String key = "links:" + userId;
        return redisOps.opsForList().range(key, 0, -1).collectList().flatMap(list -> {
            if (!list.isEmpty()) {
                return Mono.just(list);
            }
            return scrapperClient
                    .getLinksByUserId(userId)
                    .flatMapMany(Flux::fromIterable)
                    .flatMap(link -> redisOps.opsForList().rightPush(key, link).thenReturn(link))
                    .collectList()
                    .flatMap(freshList -> redisOps.expire(key, TTL).thenReturn(freshList));
        });
    }
}

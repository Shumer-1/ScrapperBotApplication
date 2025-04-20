package backend.academy.bot.config;

import backend.academy.bot.model.entities.Link;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisOperations<String, Link> linkRedisOperations(
        ReactiveRedisConnectionFactory factory,
        RedisSerializationContext<String, Link> serializationContext) {
        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    @Bean
    public RedisSerializationContext<String, Link> serializationContext() {
        StringRedisSerializer keySer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Link> valSer = new Jackson2JsonRedisSerializer<>(Link.class);
        return RedisSerializationContext.<String, Link>newSerializationContext(keySer)
            .value(valSer)
            .build();
    }
}


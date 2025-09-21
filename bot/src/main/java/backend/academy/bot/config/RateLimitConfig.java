package backend.academy.bot.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableConfigurationProperties(RateLimitProps.class)
public class RateLimitConfig {

    private final RateLimitProps props;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitConfig(RateLimitProps props) {
        this.props = props;
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(
                HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws IOException, ServletException {

                String ip = extractClientIp(req);
                String path = req.getRequestURI();
                String key = ip + "::" + path;

                Bucket bucket = buckets.computeIfAbsent(key, it -> newBucketFor(path));
                if (bucket.tryConsume(1)) {
                    chain.doFilter(req, res);
                } else {
                    RateLimitProps.RateLimitSettings s = settingsFor(path);
                    res.setStatus(s.getErrorCode());
                    res.getWriter().write(s.getErrorMessage());
                }
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    private Bucket newBucketFor(String path) {
        RateLimitProps.RateLimitSettings s = settingsFor(path);
        Refill refill = Refill.intervally(s.getRefillTokens(), s.getPeriod());
        Bandwidth limit = Bandwidth.classic(s.getCapacity(), refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private RateLimitProps.RateLimitSettings settingsFor(String path) {
        return props.getPerEndpoint().getOrDefault(path, props.getDefaults());
    }

    private String extractClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}

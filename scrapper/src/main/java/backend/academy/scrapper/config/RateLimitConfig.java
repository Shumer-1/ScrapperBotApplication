package backend.academy.scrapper.config;

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

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitProps props;

    public RateLimitConfig(RateLimitProps props) { this.props = props; }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override protected void doFilterInternal(
                HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws IOException, ServletException {

                String ip = extractClientIp(req);

                Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());
                if (bucket.tryConsume(1)) {
                    chain.doFilter(req, res);
                } else {
                    res.setStatus(props.errorCode());
                    res.getWriter().write(props.errorMessage());
                }
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }

    private Bucket newBucket() {
        Refill refill = Refill.intervally(
            props.refillTokens(), props.period());
        Bandwidth limit = Bandwidth.classic(props.capacity(), refill);
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}

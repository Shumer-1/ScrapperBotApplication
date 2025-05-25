package backend.academy.bot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProps {

    @NotNull
    private RateLimitSettings defaults;

    @NotNull
    private Map<String, RateLimitSettings> perEndpoint = new HashMap<>();

    public RateLimitSettings getDefaults() {
        return defaults;
    }

    public void setDefaults(RateLimitSettings defaults) {
        this.defaults = defaults;
    }

    public Map<String, RateLimitSettings> getPerEndpoint() {
        return perEndpoint;
    }

    public void setPerEndpoint(Map<String, RateLimitSettings> perEndpoint) {
        this.perEndpoint = perEndpoint;
    }

    @Validated
    public static class RateLimitSettings {
        @Min(1)
        private int capacity;
        @Min(1)
        private int refillTokens;
        @NotNull
        private Duration period;
        @Min(100)
        private int errorCode;
        @NotEmpty
        private String errorMessage;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public void setRefillTokens(int refillTokens) {
            this.refillTokens = refillTokens;
        }

        public Duration getPeriod() {
            return period;
        }

        public void setPeriod(Duration period) {
            this.period = period;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}

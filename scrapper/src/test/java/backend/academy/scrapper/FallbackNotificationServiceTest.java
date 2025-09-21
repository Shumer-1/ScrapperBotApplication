package backend.academy.scrapper;

import backend.academy.scrapper.service.notification.FallbackNotificationService;
import backend.academy.scrapper.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.when;

class FallbackNotificationServiceTest {

    @Test
    void shouldFallbackToKafkaWhenHttpFails() {
        NotificationService http  = Mockito.mock(NotificationService.class);
        NotificationService kafka = Mockito.mock(NotificationService.class);

        when(http.sendNotification(Mockito.anyString(), Mockito.anyLong()))
            .thenReturn(Mono.error(new RuntimeException("timeout")));
        when(kafka.sendNotification(Mockito.anyString(), Mockito.anyLong()))
            .thenReturn(Mono.empty());

        FallbackNotificationService fallback =
            new FallbackNotificationService(http, kafka, "http");

        StepVerifier.create(fallback.sendNotification("hi", 1L))
            .verifyComplete();
    }
}

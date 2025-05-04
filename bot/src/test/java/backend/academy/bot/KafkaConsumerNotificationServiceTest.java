package backend.academy.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.avro.NotificationRequest;
import backend.academy.bot.service.notification.KafkaConsumerNotificationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

class KafkaConsumerNotificationServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private KafkaConsumerNotificationService kafkaConsumerNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleExceptionWhenTelegramFails() {
        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test message");
        request.setUserId(123456789L);

        when(telegramBot.execute(any(SendMessage.class)))
            .thenThrow(new RuntimeException("Telegram API error"));

        try {
            kafkaConsumerNotificationService.listen(List.of(request), acknowledgment);
        } catch (Exception ignored) {
        }

        verify(telegramBot).execute(any(SendMessage.class));
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldProcessValidNotificationAndAcknowledge() {
        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test message");
        request.setUserId(123456789L);

        kafkaConsumerNotificationService.listen(List.of(request), acknowledgment);

        verify(telegramBot).execute(any(SendMessage.class));
        verify(acknowledgment).acknowledge();
    }
}

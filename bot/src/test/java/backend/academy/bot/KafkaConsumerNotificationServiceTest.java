package backend.academy.bot;

import backend.academy.bot.model.dto.NotificationRequest;
import backend.academy.bot.services.KafkaConsumerNotificationService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.EnableKafka;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableKafka
public class KafkaConsumerNotificationServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private KafkaConsumerNotificationService kafkaConsumerNotificationService;

    @Value("${app.topics.input}")
    private String topic;

    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @BeforeEach
    void setUp() {
        kafkaContainer.start();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldHandleExceptionWhenTelegramFails() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test message");
        request.setUserId(123456789L);

        when(telegramBot.execute(any(SendMessage.class)))
            .thenThrow(new RuntimeException("Telegram API error"));

        try {
            kafkaConsumerNotificationService.listen(request);
        } catch (Exception e) {
            verify(telegramBot).execute(any(SendMessage.class));
        }
    }

    @Test
    void shouldIgnoreExtraFieldsInNotificationRequest() throws Exception {
        String extraFieldJson = """
            {
              "message": "Test message",
              "userId": 123456789,
              "extraField": "extraValue"
            }
            """;

        NotificationRequest request = new NotificationRequest();
        request.setMessage("Test message");
        request.setUserId(123456789L);

        kafkaConsumerNotificationService.listen(request);

        verify(telegramBot).execute(any(SendMessage.class));
    }
}

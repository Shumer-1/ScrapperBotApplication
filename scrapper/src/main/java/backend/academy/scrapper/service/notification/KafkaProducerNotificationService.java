package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.model.dto.NotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "Kafka")
public class KafkaProducerNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerNotificationService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.topics.output}")
    private String topic;

    public KafkaProducerNotificationService(KafkaTemplate<String, String> kafkaTemplate,
                                            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    private void sendMessage(String message) {
        kafkaTemplate.send(topic, message);
    }

    @Override
    public void sendNotification(String message, long userId) {
        NotificationRequest notification = new NotificationRequest(message, userId);
        try {
            String jsonMessage = objectMapper.writeValueAsString(notification);
            sendMessage(jsonMessage);
            log.info("Уведомление отправлено в Kafka: {}", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при сериализации уведомления в JSON", e);
        }
    }
}

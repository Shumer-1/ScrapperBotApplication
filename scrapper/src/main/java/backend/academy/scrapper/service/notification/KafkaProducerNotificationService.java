package backend.academy.scrapper.service.notification;

import backend.academy.avro.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.message-transport", havingValue = "kafka", matchIfMissing = true)
public class KafkaProducerNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerNotificationService.class);
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${app.topics.output}")
    private String topic;

    public KafkaProducerNotificationService(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendNotification(String message, long userId) {
        NotificationRequest record = NotificationRequest.newBuilder()
                .setMessage(message)
                .setUserId(userId)
                .build();

        kafkaTemplate.send(topic, record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Ошибка при отправке Avro-сообщения", ex);
            } else {
                log.info("Avro-сообщение отправлено: {}", record);
            }
        });
    }
}

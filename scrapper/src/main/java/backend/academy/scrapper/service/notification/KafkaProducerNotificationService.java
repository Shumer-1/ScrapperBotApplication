package backend.academy.scrapper.service.notification;

import backend.academy.avro.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("kafkaNotificationService")
public class KafkaProducerNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerNotificationService.class);
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    @Value("${app.topics.output}")
    private String topic;

    public KafkaProducerNotificationService(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<Void> sendNotification(String message, long userId) {
        NotificationRequest record = NotificationRequest.newBuilder()
            .setMessage(message)
            .setUserId(userId)
            .build();

        return Mono.fromFuture(kafkaTemplate.send(topic, record))
            .doOnSuccess(result -> log.info("Kafka-сообщение отправлено offset={}",
                result.getRecordMetadata().offset()))
            .doOnError(error -> log.error("Ошибка при отправке Avro-сообщения", error))
            .then();
    }

}

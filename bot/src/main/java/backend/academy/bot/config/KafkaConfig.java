// src/main/java/backend/academy/bot/config/KafkaConfig.java
package backend.academy.bot.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConfig {

    @Value("${app.topics.input}")
    private String inputTopic;

    @Value("${app.topics.dead-letter}")
    private String deadLetterTopic;

    @Bean
    public NewTopic inputTopic() {
        return TopicBuilder.name(inputTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<Object, Object> template) {
        return new DeadLetterPublishingRecoverer(template,
            (record, ex) -> new TopicPartition(deadLetterTopic, record.partition()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
        ConsumerFactory<Object, Object> cf,
        DeadLetterPublishingRecoverer recoverer,
        @Value("${app.listener.concurrency}") int concurrency) {

        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(concurrency);
        factory.setBatchListener(true);

        var props = factory.getContainerProperties();
        props.setAckMode(AckMode.MANUAL_IMMEDIATE);
        props.setPollTimeout(1_000);
        props.setIdleBetweenPolls(500);
        props.setObservationEnabled(true);

        var backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(500);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(10_000);

        var eh = new DefaultErrorHandler(recoverer, backOff);
        eh.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(eh);
        return factory;
    }
}

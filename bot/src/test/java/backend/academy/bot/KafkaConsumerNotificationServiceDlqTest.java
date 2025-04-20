// src/test/java/backend/academy/bot/KafkaConsumerNotificationServiceDlqTest.java
package backend.academy.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
class KafkaConsumerNotificationServiceDlqTest {

    static final String TOPIC = "scrapperTopic";
    static final String DLQ_TOPIC = "deadLetterTopic";

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry reg) {
        reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        reg.add("app.topics.input", () -> TOPIC);
        reg.add("app.topics.dead-letter", () -> DLQ_TOPIC);
    }

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    Consumer<String, String> consumer;

    @BeforeEach
    void setup() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
            kafka.getBootstrapServers(), "test-group", "true");
        consumer = new DefaultKafkaConsumerFactory<>(
            props, new StringDeserializer(), new StringDeserializer())
            .createConsumer();
        consumer.subscribe(List.of(DLQ_TOPIC));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void whenNullField_thenGoesToDlq() throws Exception {
        String badJson = "{\"userId\":123,\"message\":null}";
        kafkaTemplate.send(TOPIC, "123", badJson);
        kafkaTemplate.flush();

        ConsumerRecord<String, String> rec = KafkaTestUtils.getSingleRecord(
            consumer, DLQ_TOPIC, Duration.ofSeconds(10));
        assertNotNull(rec);

        String rawValue = rec.value();
        if (rawValue != null && rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            rawValue = new ObjectMapper().readValue(rawValue, String.class);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected = mapper.readTree(badJson);
        JsonNode actual = mapper.readTree(rawValue);

        assertEquals(expected, actual);
    }
}

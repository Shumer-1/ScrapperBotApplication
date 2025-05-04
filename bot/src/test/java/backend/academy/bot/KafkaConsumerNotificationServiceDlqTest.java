//package backend.academy.bot;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import backend.academy.avro.NotificationRequest;
//import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
//import io.confluent.kafka.serializers.KafkaAvroDeserializer;
//import io.confluent.kafka.serializers.KafkaAvroSerializer;
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.KafkaContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import org.testcontainers.utility.DockerImageName;
//
//@SpringBootTest
//@Testcontainers
//class KafkaConsumerNotificationServiceDlqTest {
//
//    private static final String TOPIC = "scrapperTopic";
//    private static final String DLQ_TOPIC = "deadLetterTopic";
//    private static final String MOCK_SCHEMA_REGISTRY = "mock://bot-tests";
//
//    @Container
//    static KafkaContainer kafka =
//        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));
//
//    /* ---------- переопределяем конфигурацию Spring на время теста ---------- */
//    @DynamicPropertySource
//    static void springKafkaProps(DynamicPropertyRegistry reg) {
//        reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
//        reg.add("app.topics.input", () -> TOPIC);
//        reg.add("app.topics.dead-letter", () -> DLQ_TOPIC);
//
//        // продюсер Bot-приложения и DLQ-продюсер
//        reg.add("spring.kafka.producer.value-serializer",
//            () -> KafkaAvroSerializer.class.getName());
//        reg.add("spring.kafka.producer.properties." +
//                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
//            () -> MOCK_SCHEMA_REGISTRY);
//
//        // слушатель Bot-приложения
//        reg.add("spring.kafka.consumer.value-deserializer",
//            () -> KafkaAvroDeserializer.class.getName());
//        reg.add("spring.kafka.consumer.properties." +
//                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
//            () -> MOCK_SCHEMA_REGISTRY);
//        reg.add("spring.kafka.consumer.properties.specific.avro.reader", () -> "true");
//    }
//
//    /* ---------- собственный консюмер, чтобы читать DLQ ---------- */
//    private Consumer<String, NotificationRequest> consumer;
//
//    @BeforeEach
//    void setUp() {
//        Map<String, Object> props =
//            KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-group", "true");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
//        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY);
//        props.put("specific.avro.reader", true);
//
//        consumer = new DefaultKafkaConsumerFactory<String, NotificationRequest>(props).createConsumer();
//        consumer.subscribe(List.of(DLQ_TOPIC));
//    }
//
//    @AfterEach
//    void tearDown() {
//        consumer.close();
//    }
//
//    /* ---------- сам тест ---------- */
//    @Test
//    void whenNullField_thenRecordAppearsInDlq() {
//        /* создаём Avro-объект с null-полем message */
//        NotificationRequest bad = NotificationRequest.newBuilder()
//            .setUserId(123L)
//            .setMessage(null)
//            .build();
//
//        /* шлём его напрямую продюсером с Avro-сериализатором */
//        Map<String, Object> prodCfg = new HashMap<>();
//        prodCfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
//        prodCfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        prodCfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
//        prodCfg.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY);
//
//        try (KafkaProducer<String, NotificationRequest> producer =
//                 new KafkaProducer<>(prodCfg)) {
//            producer.send(new org.apache.kafka.clients.producer.ProducerRecord<>(TOPIC, null, bad));
//            producer.flush();
//        }
//
//        /* ждём появления записи в DLQ */
//        ConsumerRecord<String, NotificationRequest> rec =
//            KafkaTestUtils.getSingleRecord(consumer, DLQ_TOPIC, Duration.ofSeconds(20));
//        assertNotNull(rec, "Запись в DLQ не найдена");
//
//        NotificationRequest actual = rec.value();
//        assertAll(
//            () -> assertEquals(bad.getUserId(), actual.getUserId()),
//            () -> assertNull(actual.getMessage(), "Поле message должно быть null")
//        );
//    }
//}

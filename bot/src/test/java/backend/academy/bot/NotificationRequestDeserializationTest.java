package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.model.dto.NotificationRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class NotificationRequestDeserializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @MethodSource("jsonDtoProvider")
    void shouldDeserializeJson(
            String json, String expectedMessage, long expectedUserId, boolean ignoreUnknownProperties)
            throws Exception {
        if (ignoreUnknownProperties) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        NotificationRequest dto = objectMapper.readValue(json, NotificationRequest.class);

        assertNotNull(dto, "DTO не должен быть null");
        assertEquals(expectedMessage, dto.getMessage());
        assertEquals(expectedUserId, dto.getUserId());
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> jsonDtoProvider() {
        String fullJson =
                """
            {
              "message": "Hello!",
              "userId": 123456789
            }
            """;
        String extraFieldJson =
                """
            {
              "message": "Test",
              "userId": 1,
              "extraField": "value"
            }
            """;

        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(fullJson, "Hello!", 123456789L, false),
                org.junit.jupiter.params.provider.Arguments.of(extraFieldJson, "Test", 1L, true));
    }
}

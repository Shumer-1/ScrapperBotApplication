package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.data.jdbcRepositories.JdbcUserRepository;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.service.userService.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {"access-type=SQL", "spring.jpa.hibernate.ddl-auto=update"})
public class JdbcUserServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test_db")
            .withUsername("admin")
            .withPassword("12345");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcUserRepository userRepository;

    @Test
    public void testSaveNewUser() {
        long telegramId = 654321L;
        String username = "jdbcuser";
        boolean saved = userService.save(telegramId, username);

        User user = userService.getUserByTelegramId(telegramId);
        System.out.println(user.getTelegramId());
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    public void testSaveExistingUser() {
        long telegramId = 654321L;
        String username = "jdbcuser";
        boolean saved = userService.save(telegramId, username);
        assertThat(saved).isTrue();

        boolean savedAgain = userService.save(telegramId, username);
        assertThat(savedAgain).isFalse();
    }

    @Test
    public void testUserNotFound() {
        long telegramId = 888888L;
        Exception exception =
                assertThrows(IllegalArgumentException.class, () -> userService.getUserByTelegramId(telegramId));
        assertThat(exception.getMessage()).contains("Пользователь не найден");
    }
}

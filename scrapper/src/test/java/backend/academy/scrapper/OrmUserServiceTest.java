package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.data.ormRepositories.OrmUserRepository;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.service.userService.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {"access-type=ORM", "spring.jpa.hibernate.ddl-auto=update"})
public class OrmUserServiceTest {

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
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private OrmUserRepository userRepository;

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void testSaveNewUser() {
        long telegramId = 123456L;
        String username = "testuser";
        userService.save(telegramId, username);

        User user = userService.getUserByTelegramId(telegramId);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    public void testSaveExistingUser() {
        long telegramId = 123456L;
        String username = "testuser";
        userService.save(telegramId, username);
        assertThrows(UserAlreadyExistsException.class, () -> userService.save(telegramId, username));
    }

    @Test
    public void testUserNotFound() {
        long telegramId = 999999L;
        Exception exception =
                assertThrows(IllegalArgumentException.class, () -> userService.getUserByTelegramId(telegramId));
        assertThat(exception.getMessage()).contains("Пользователь не найден");
    }
}

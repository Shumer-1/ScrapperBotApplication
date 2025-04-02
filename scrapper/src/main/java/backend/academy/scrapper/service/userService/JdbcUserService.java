package backend.academy.scrapper.service.userService;

import backend.academy.scrapper.data.jdbcRepositories.JdbcUserRepository;
import backend.academy.scrapper.model.entities.User;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcUserService implements UserService {

    private final JdbcUserRepository userRepository;

    public JdbcUserService(JdbcUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public boolean save(long telegramId, String username) {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        if (existingUser.isPresent()) {
            return false;
        }
        User user = new User();
        user.setTelegramId(telegramId);
        user.setUsername(username);
        userRepository.saveUser(user);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTelegramId(long telegramId) {
        return userRepository.findByTelegramId(telegramId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByTelegramId(long telegramId) {
        return userRepository
                .findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}

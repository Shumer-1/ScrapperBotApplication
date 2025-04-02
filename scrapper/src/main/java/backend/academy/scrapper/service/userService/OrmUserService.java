package backend.academy.scrapper.service.userService;

import backend.academy.scrapper.data.ormRepositories.OrmUserRepository;
import backend.academy.scrapper.model.entities.User;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public class OrmUserService implements UserService {

    @Autowired
    private final OrmUserRepository userRepository;

    public OrmUserService(OrmUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public boolean save(long telegramId, String username) {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        if (existingUser.isPresent()) {
            return false;
        }
        User user = new User(telegramId, username, new CopyOnWriteArrayList<>());
        userRepository.save(user);
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

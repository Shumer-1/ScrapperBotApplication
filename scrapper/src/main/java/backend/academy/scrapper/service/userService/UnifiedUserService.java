package backend.academy.scrapper.service.userService;

import backend.academy.scrapper.data.UserRepository;
import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.model.entities.User;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnifiedUserService implements UserService {

    @Autowired
    private final UserRepository userRepository;

    public UnifiedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void save(long telegramId, String username) throws UserAlreadyExistsException {
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с telegramId " + telegramId + " уже существует");
        }
        User user = new User(telegramId, username, new CopyOnWriteArrayList<>());
        userRepository.saveUser(user);
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

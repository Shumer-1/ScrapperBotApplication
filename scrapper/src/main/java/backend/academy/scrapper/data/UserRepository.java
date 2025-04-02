package backend.academy.scrapper.data;

import backend.academy.scrapper.model.entities.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByTelegramId(Long telegramId);

    User saveUser(User user);
}

package backend.academy.scrapper.service.userService;

import backend.academy.scrapper.model.entities.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    boolean save(long telegramId, String username);

    boolean existsByTelegramId(long userId);

    User getUserByTelegramId(long id);
}

package backend.academy.scrapper.service.userService;

import backend.academy.scrapper.exceptions.UserAlreadyExistsException;
import backend.academy.scrapper.model.entities.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    void save(long telegramId, String username) throws UserAlreadyExistsException;

    boolean existsByTelegramId(long userId);

    User getUserByTelegramId(long id);
}

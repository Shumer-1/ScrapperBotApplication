package backend.academy.bot.service;

import backend.academy.bot.data.UserRepository;
import backend.academy.bot.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean registerUser(long telegramId, String username, String firstName, String lastName) {
        User user = userRepository.findByTelegramId(telegramId);
        if (user == null) {
            user = new User(telegramId, username, firstName, lastName);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}

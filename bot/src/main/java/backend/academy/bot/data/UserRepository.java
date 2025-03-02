package backend.academy.bot.data;

import backend.academy.bot.model.User;

public interface UserRepository {

    User findByTelegramId(long telegramId);

    User save(User user);
}

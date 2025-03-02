package backend.academy.bot.data;

import backend.academy.bot.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage;

    public InMemoryUserRepository() {
        this.storage = new ConcurrentHashMap<>();
    }

    @Override
    public User findByTelegramId(long telegramId) {
        return storage.get(telegramId);
    }

    @Override
    public User save(User user) {
        storage.put(user.getTelegramId(), user);
        return user;
    }
}

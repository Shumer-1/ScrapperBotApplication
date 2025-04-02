package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.data.UserRepository;
import backend.academy.scrapper.model.entities.User;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public interface OrmUserRepository extends JpaRepository<User, Long>, UserRepository {
    Optional<User> findByTelegramId(Long telegramId);

    default User saveUser(User user) {
        return save(user);
    }
}

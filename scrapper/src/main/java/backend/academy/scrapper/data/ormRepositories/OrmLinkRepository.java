package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.data.LinkRepository;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public interface OrmLinkRepository extends JpaRepository<Link, Long>, LinkRepository {

    String USER_TELEGRAM_FILTER = "l.user.telegramId = :userId";
    String LINK_NAME_FILTER = "l.link = :linkName";

    List<Link> findByUserId(Long userId);

    default Link saveLink(Link link) {
        return save(link);
    }

    @Modifying
    void delete(Link link);

    @Query("SELECT l FROM Link l WHERE " + USER_TELEGRAM_FILTER)
    List<Link> findByTelegramId(@Param("userId") Long userId);

    @Override
    default List<Link> findByUserTelegramId(long telegramId) {
        return findByTelegramId(telegramId);
    }

    @Query("SELECT l FROM Link l WHERE " + USER_TELEGRAM_FILTER + " AND " + LINK_NAME_FILTER)
    Link findByUserIdAndLink(@Param("userId") Long userId, @Param("linkName") String link);

    @Modifying
    @Query("UPDATE Link l SET l.lastUpdated = :time WHERE " + LINK_NAME_FILTER + " AND " + USER_TELEGRAM_FILTER)
    void refreshLastUpdated(
            @Param("linkName") String linkName, @Param("userId") long userId, @Param("time") Instant time);

    @Query(
            """
        SELECT l
        FROM Link l
        JOIN l.user u
        JOIN l.tags t
        WHERE u.telegramId = :userId AND t = :tag
        """)
    List<Link> findByTelegramIdAndTag(@Param("tag") Tag tag, @Param("userId") long userId);
}

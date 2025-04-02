package backend.academy.scrapper.data;

import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkRepository {
    List<Link> findByUserTelegramId(long userId);

    Link findByUserIdAndLink(Long userId, String link);

    Link saveLink(Link link);

    void delete(Link link);

    void refreshLastUpdated(String linkName, long userId, Instant time);

    List<Link> findByTelegramIdAndTag(Tag tag, long userId);

    Page<Link> findAll(Pageable pageable);
}

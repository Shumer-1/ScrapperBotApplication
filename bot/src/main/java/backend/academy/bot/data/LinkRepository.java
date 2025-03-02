package backend.academy.bot.data;

import backend.academy.bot.model.Link;
import java.util.List;

public interface LinkRepository {
    Link findByLink(String link);

    boolean existsByLink(String link);

    boolean deleteLinkByUserIdAndLink(long userId, String link);

    List<Link> findLinksByUserId(long userId);

    boolean save(Link link);
}

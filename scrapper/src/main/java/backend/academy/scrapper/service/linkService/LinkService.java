package backend.academy.scrapper.service.linkService;

import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;

public interface LinkService {

    void addLink(String link, Long userId, Set<String> tags, Set<String> filters);

    List<Link> getLinksByUserId(Long userId);

    void deleteLinkByUserIdAndLink(Long userId, String link);

    void saveTag(Tag tag);

    void saveFilter(Filter filter);

    boolean containsTag(Tag tag);

    boolean containsFilter(Filter filter);

    Tag getTag(String tagName);

    Filter getFilter(String filterName);

    Page<Link> getTrackingPage(int page, int size);

    void refreshLastUpdated(String linkName, long userId, Instant time);

    List<Link> findLinksByTagAndUserId(Tag tag, long userId);
}

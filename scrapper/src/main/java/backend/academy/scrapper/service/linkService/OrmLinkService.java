package backend.academy.scrapper.service.linkService;

import backend.academy.scrapper.data.ormRepositories.OrmFilterRepository;
import backend.academy.scrapper.data.ormRepositories.OrmLinkRepository;
import backend.academy.scrapper.data.ormRepositories.OrmTagRepository;
import backend.academy.scrapper.data.ormRepositories.OrmUserRepository;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public class OrmLinkService implements LinkService {
    private static final Logger log = LoggerFactory.getLogger(OrmLinkService.class);
    private final OrmLinkRepository linkRepository;
    private final OrmUserRepository userRepository;
    private final OrmTagRepository tagRepository;
    private final OrmFilterRepository filterRepository;

    public OrmLinkService(
            OrmLinkRepository linkRepository,
            OrmUserRepository userRepository,
            OrmTagRepository tagRepository,
            OrmFilterRepository filterRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.filterRepository = filterRepository;
    }

    @Transactional
    public void addLink(String linkUrl, Long userId, Set<String> tagNames, Set<String> filterNames) {
        User user = userRepository
                .findByTelegramId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Tag> tags = tagNames.stream()
                .map(tagName -> tagRepository.findByTag(tagName).orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toSet());

        Set<Filter> filters = filterNames.stream()
                .map(filterName -> filterRepository
                        .findByFilter(filterName)
                        .orElseGet(() -> filterRepository.save(new Filter(filterName))))
                .collect(Collectors.toSet());

        Link newLink = new Link();
        newLink.setLink(linkUrl);
        newLink.setUser(user);
        newLink.setTags(tags);
        newLink.setFilters(filters);

        linkRepository.save(newLink);
    }

    @Transactional(readOnly = true)
    public List<Link> getLinksByUserId(Long userId) {
        return linkRepository.findByTelegramId(userId);
    }

    @Transactional
    public boolean deleteLinkByUserIdAndLink(Long userId, String linkUrl) {
        Link link = linkRepository.findByUserIdAndLink(userId, linkUrl);
        if (link != null) {
            linkRepository.delete(link);
            return true;
        }
        return false;
    }

    @Transactional
    public void saveTag(Tag tag) {
        if (!tagRepository.findByTag(tag.getTag()).isPresent()) {
            tagRepository.save(tag);
        }
    }

    @Transactional
    public void saveFilter(Filter filter) {
        if (!filterRepository.findByFilter(filter.getFilter()).isPresent()) {
            filterRepository.save(filter);
        }
    }

    @Transactional
    public boolean containsTag(Tag tag) {
        return tagRepository.findByTag(tag.getTag()).isPresent();
    }

    @Transactional
    public boolean containsFilter(Filter filter) {
        return filterRepository.findByFilter(filter.getFilter()).isPresent();
    }

    @Transactional
    public Tag getTag(String tagName) {
        Optional<Tag> tag = tagRepository.findByTag(tagName);
        return tag.orElse(null);
    }

    @Transactional
    public Filter getFilter(String filterName) {
        Optional<Filter> filter = filterRepository.findByFilter(filterName);
        return filter.orElse(null);
    }

    @Transactional
    public Page<Link> getTrackingPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return linkRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void refreshLastUpdated(String linkName, long userId, Instant time) {
        log.info("Обновляем время на {} у {} для {}", time, linkName, userId);
        linkRepository.refreshLastUpdated(linkName, userId, time);
    }

    @Transactional
    public List<Link> findLinksByTagAndUserId(Tag tag, long userId) {
        return linkRepository.findByTelegramIdAndTag(tag, userId);
    }
}

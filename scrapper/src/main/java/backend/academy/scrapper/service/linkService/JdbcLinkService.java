package backend.academy.scrapper.service.linkService;

import backend.academy.scrapper.data.jdbcRepositories.JdbcFilterRepository;
import backend.academy.scrapper.data.jdbcRepositories.JdbcLinkRepository;
import backend.academy.scrapper.data.jdbcRepositories.JdbcTagRepository;
import backend.academy.scrapper.data.jdbcRepositories.JdbcUserRepository;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;
    private final JdbcUserRepository userRepository;
    private final JdbcTagRepository tagRepository;
    private final JdbcFilterRepository filterRepository;

    public JdbcLinkService(
            JdbcLinkRepository linkRepository,
            JdbcUserRepository userRepository,
            JdbcTagRepository tagRepository,
            JdbcFilterRepository filterRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.filterRepository = filterRepository;
    }

    @Transactional
    public void addLink(String linkUrl, Long telegramId, Set<String> tagNames, Set<String> filterNames) {
        User user = userRepository
                .findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Set<Tag> tags = tagNames.stream()
                .map(tagName -> tagRepository.findByTag(tagName).orElseGet(() -> tagRepository.save(new Tag(tagName))))
                .collect(Collectors.toSet());

        Set<Filter> filters = filterNames.stream()
                .map(filterName -> filterRepository
                        .findByFilter(filterName)
                        .orElseGet(() -> filterRepository.save(new Filter(filterName))))
                .collect(Collectors.toSet());

        Link link = new Link();
        link.setLink(linkUrl);
        link.setUser(user);
        link.setTags(tags);
        link.setFilters(filters);

        linkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<Link> getLinksByUserId(Long telegramId) {
        return linkRepository.findByUserId(telegramId);
    }

    @Transactional
    public boolean deleteLinkByUserIdAndLink(Long telegramId, String linkUrl) {
        Link link = linkRepository.findByUserIdAndLink(telegramId, linkUrl);
        if (link != null) {
            linkRepository.delete(link);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void saveTag(Tag tag) {
        if (!tagRepository.findByTag(tag.getTag()).isPresent()) {
            tagRepository.save(tag);
        }
    }

    @Override
    @Transactional
    public void saveFilter(Filter filter) {
        if (!filterRepository.findByFilter(filter.getFilter()).isPresent()) {
            filterRepository.save(filter);
        }
    }

    @Override
    public boolean containsTag(Tag tag) {
        return tagRepository.findByTag(tag.getTag()).isPresent();
    }

    @Override
    public boolean containsFilter(Filter filter) {
        return filterRepository.findByFilter(filter.getFilter()).isPresent();
    }

    public Tag getTag(String tagName) {
        Optional<Tag> tag = tagRepository.findByTag(tagName);
        return tag.orElse(null);
    }

    public Filter getFilter(String filterName) {
        Optional<Filter> filter = filterRepository.findByFilter(filterName);
        return filter.orElse(null);
    }

    @Override
    public Page<Link> getTrackingPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return linkRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public void refreshLastUpdated(String linkName, long userId, Instant time) {
        linkRepository.refreshLastUpdated(linkName, userId, time);
    }

    @Override
    public List<Link> findLinksByTagAndUserId(Tag tag, long userId) {
        return linkRepository.findByTagAndUserId(userId, tag);
    }
}

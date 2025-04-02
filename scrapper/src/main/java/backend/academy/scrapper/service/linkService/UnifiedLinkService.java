package backend.academy.scrapper.service.linkService;

import backend.academy.scrapper.data.FilterRepository;
import backend.academy.scrapper.data.LinkRepository;
import backend.academy.scrapper.data.TagRepository;
import backend.academy.scrapper.data.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UnifiedLinkService implements LinkService {
    private static final Logger log = LoggerFactory.getLogger(UnifiedLinkService.class);
    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    public UnifiedLinkService(
            LinkRepository linkRepository,
            UserRepository userRepository,
            TagRepository tagRepository,
            FilterRepository filterRepository) {
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.filterRepository = filterRepository;
    }

    private Tag getOrCreateTag(String tagName) {
        return tagRepository.findByTag(tagName).orElseGet(() -> tagRepository.save(new Tag(tagName)));
    }

    private Filter getOrCreateFilter(String filterName) {
        return filterRepository.findByFilter(filterName).orElseGet(() -> filterRepository.save(new Filter(filterName)));
    }

    @Override
    @Transactional
    public void addLink(String linkUrl, Long userId, Set<String> tagNames, Set<String> filterNames) {
        User user = userRepository
                .findByTelegramId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Tag> tags = tagNames.stream().map(this::getOrCreateTag).collect(Collectors.toSet());

        Set<Filter> filters = filterNames.stream().map(this::getOrCreateFilter).collect(Collectors.toSet());

        Link newLink = new Link();
        newLink.setLink(linkUrl);
        newLink.setUser(user);
        newLink.setTags(tags);
        newLink.setFilters(filters);

        linkRepository.saveLink(newLink);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Link> getLinksByUserId(Long userId) {
        return linkRepository.findByUserTelegramId(userId);
    }

    @Override
    @Transactional
    public boolean deleteLinkByUserIdAndLink(Long userId, String linkUrl) {
        Link link = linkRepository.findByUserIdAndLink(userId, linkUrl);
        if (link != null) {
            linkRepository.delete(link);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void saveTag(Tag tag) {
        getOrCreateTag(tag.getTag());
    }

    @Override
    @Transactional
    public void saveFilter(Filter filter) {
        getOrCreateFilter(filter.getFilter());
    }

    @Override
    @Transactional
    public boolean containsTag(Tag tag) {
        return tagRepository.findByTag(tag.getTag()).isPresent();
    }

    @Override
    @Transactional
    public boolean containsFilter(Filter filter) {
        return filterRepository.findByFilter(filter.getFilter()).isPresent();
    }

    @Override
    @Transactional
    public Tag getTag(String tagName) {
        Optional<Tag> tag = tagRepository.findByTag(tagName);
        return tag.orElse(null);
    }

    @Override
    @Transactional
    public Filter getFilter(String filterName) {
        Optional<Filter> filter = filterRepository.findByFilter(filterName);
        return filter.orElse(null);
    }

    @Override
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

    @Override
    @Transactional
    public List<Link> findLinksByTagAndUserId(Tag tag, long userId) {
        return linkRepository.findByTelegramIdAndTag(tag, userId);
    }
}

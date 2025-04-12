package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.data.ormRepositories.OrmFilterRepository;
import backend.academy.scrapper.data.ormRepositories.OrmLinkRepository;
import backend.academy.scrapper.data.ormRepositories.OrmTagRepository;
import backend.academy.scrapper.data.ormRepositories.OrmUserRepository;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.service.linkService.LinkService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = {"access-type=ORM", "spring.jpa.hibernate.ddl-auto=update"})
public class OrmLinkServiceTest {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test_db")
            .withUsername("admin")
            .withPassword("12345");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private LinkService linkService;

    @Autowired
    private OrmLinkRepository linkRepository;

    @Autowired
    private OrmUserRepository userRepository;

    @Autowired
    private OrmTagRepository tagRepository;

    @Autowired
    private OrmFilterRepository filterRepository;

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setTelegramId(111L);
        user.setUsername("testuser");
        userRepository.save(user);
    }

    @AfterEach
    public void cleanup() {
        linkRepository.deleteAll();
        tagRepository.deleteAll();
        filterRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testAddLink() {
        Long userId = 111L;
        String linkUrl = "http://example.com";
        Set<String> tagNames = Set.of("tag1", "tag2");
        Set<String> filterNames = Set.of("filter1", "filter2");

        linkService.addLink(linkUrl, userId, tagNames, filterNames);

        List<Link> links = linkRepository.findByTelegramId(userId);
        assertThat(links).hasSize(1);

        Link link = links.get(0);
        assertThat(link.getLink()).isEqualTo(linkUrl);
        assertThat(link.getTags()).extracting("tag").containsExactlyInAnyOrder("tag1", "tag2");
        assertThat(link.getFilters()).extracting("filter").containsExactlyInAnyOrder("filter1", "filter2");
    }

    @Test
    public void testGetLinksByUserId() {
        Long userId = 111L;
        linkService.addLink("http://example.com/1", userId, Set.of("tagA"), Set.of("filterA"));
        linkService.addLink("http://example.com/2", userId, Set.of("tagB"), Set.of("filterB"));

        List<Link> links = linkService.getLinksByUserId(userId);
        assertThat(links).hasSize(2);
    }

    @Test
    public void testDeleteLinkByUserIdAndLink() {
        Long userId = 111L;
        String linkUrl = "http://example.com/delete";
        linkService.addLink(linkUrl, userId, Set.of("tagX"), Set.of("filterX"));

        assertThatCode(() -> linkService.deleteLinkByUserIdAndLink(userId, linkUrl))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> linkService.deleteLinkByUserIdAndLink(userId, linkUrl))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не существует");
    }

    @Test
    public void testSaveTagAndContainsTag() {
        Tag tag = new Tag("newTag");
        linkService.saveTag(tag);
        boolean exists = linkService.containsTag(tag);
        assertThat(exists).isTrue();
    }

    @Test
    public void testSaveFilterAndContainsFilter() {
        Filter filter = new Filter("newFilter");
        linkService.saveFilter(filter);
        boolean exists = linkService.containsFilter(filter);
        assertThat(exists).isTrue();
    }

    @Test
    public void testGetTagAndFilter() {
        Tag tag = new Tag("testTag");
        linkService.saveTag(tag);

        Filter filter = new Filter("testFilter");
        linkService.saveFilter(filter);

        Tag foundTag = linkService.getTag("testTag");
        Filter foundFilter = linkService.getFilter("testFilter");

        assertThat(foundTag).isNotNull();
        assertThat(foundTag.getTag()).isEqualTo("testTag");

        assertThat(foundFilter).isNotNull();
        assertThat(foundFilter.getFilter()).isEqualTo("testFilter");
    }

    @Test
    public void testGetTrackingPage() {
        Long userId = 111L;
        // Добавляем 5 ссылок для пользователя
        for (int i = 1; i <= 5; i++) {
            linkService.addLink("http://example.com/" + i, userId, Set.of("tag" + i), Set.of("filter" + i));
        }
        Page<Link> page = linkService.getTrackingPage(0, 3);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    public void testRefreshLastUpdated() {
        Long userId = 111L;
        String linkUrl = "http://example.com/refresh";
        linkService.addLink(linkUrl, userId, Set.of("tagRefresh"), Set.of("filterRefresh"));
        Instant now = Instant.now();
        linkService.refreshLastUpdated(linkUrl, userId, now);
        Link link = linkRepository.findByUserIdAndLink(userId, linkUrl);
        assertThat(link.getLastUpdated().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @Transactional
    public void testFindLinksByTagAndUserId() {
        Long userId = 111L;
        String linkUrl = "http://example.com/find";
        linkService.addLink(linkUrl, userId, Set.of("commonTag"), Set.of("filterFind"));
        Tag persistedTag = linkService.getTag("commonTag");
        List<Link> links = linkService.findLinksByTagAndUserId(persistedTag, userId);
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getLink()).isEqualTo(linkUrl);
    }
}

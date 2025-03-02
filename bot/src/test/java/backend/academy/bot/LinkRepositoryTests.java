package backend.academy.bot;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.bot.data.InMemoryLinkRepository;
import backend.academy.bot.data.LinkRepository;
import backend.academy.bot.model.Link;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LinkRepositoryTests {

    @ParameterizedTest
    @MethodSource("getArgumentsForFindLinksByUserId")
    void findLinksByUserIdTest(List<Link> links, int count, long userId) {
        LinkRepository linkRepository = new InMemoryLinkRepository();
        for (var el : links) {
            linkRepository.save(el);
        }

        var list = linkRepository.findLinksByUserId(userId);
        assertThat(list.size()).isEqualTo(count);
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForDeleteLinkByUserIdAndLinkTest")
    void deleteLinkByUserIdAndLinkTest(List<Link> links, String link, long userId) {
        LinkRepository linkRepository = new InMemoryLinkRepository();
        for (var el : links) {
            linkRepository.save(el);
        }
        linkRepository.deleteLinkByUserIdAndLink(userId, link);
        assertThat(linkRepository.existsByLink(link)).isEqualTo(false);
    }

    private static Stream<Arguments> getArgumentsForDeleteLinkByUserIdAndLinkTest() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                new Link("/link", 101, 1, List.of("tag1", "tag2"), List.of("filter")),
                                new Link("/link", 101, 1, List.of("tag1", "tag2"), List.of("filter"))),
                        "/link",
                        101),
                Arguments.of(
                        List.of(
                                new Link("/link2", 1000, 120, List.of(), List.of("filter")),
                                new Link("/link3", 1000, 120, List.of(), List.of("filter1"))),
                        "/link2",
                        1000),
                Arguments.of(List.of(new Link("/link", 101, 2, List.of("tag1", "tag2"), List.of())), "/link", 101));
    }

    private static Stream<Arguments> getArgumentsForFindLinksByUserId() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                new Link("/link", 101, 1, List.of("tag1", "tag2"), List.of("filter")),
                                new Link("/link", 101, 1, List.of("tag1", "tag2"), List.of("filter"))),
                        1,
                        101L),
                Arguments.of(
                        List.of(
                                new Link("/link2", 1000, 120, List.of(), List.of("filter")),
                                new Link("/link3", 1000, 120, List.of(), List.of("filter1"))),
                        2,
                        1000L),
                Arguments.of(List.of(new Link("/link", 101, 2, List.of("tag1", "tag2"), List.of())), 1, 101L));
    }
}

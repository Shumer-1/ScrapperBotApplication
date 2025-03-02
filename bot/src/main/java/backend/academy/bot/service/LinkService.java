package backend.academy.bot.service;

import backend.academy.bot.data.LinkRepository;
import backend.academy.bot.model.Link;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final LinkRepository linkRepository;
    private AtomicLong id;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
        id = new AtomicLong(0);
    }

    public void addLink(String link, long userId, List<String> tags, List<String> filters) {
        linkRepository.save(new Link(link, userId, id.getAndAdd(1), tags, filters));
    }

    public List<Link> getLinksByUserId(long userId) {
        return linkRepository.findLinksByUserId(userId);
    }

    public boolean deleteLinkByUserIdAndLink(long userId, String link) {
        return linkRepository.deleteLinkByUserIdAndLink(userId, link);
    }
}

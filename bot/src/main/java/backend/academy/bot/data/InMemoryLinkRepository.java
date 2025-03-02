package backend.academy.bot.data;

import backend.academy.bot.model.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {
    private List<Link> storage;

    public InMemoryLinkRepository() {
        storage = new CopyOnWriteArrayList<>();
    }

    @Override
    public List<Link> findLinksByUserId(long userId) {
        List<Link> links = new ArrayList<>();

        for (var el : storage) {
            if (el.userId() == userId) {
                links.add(el);
            }
        }
        return links;
    }

    @Override
    public Link findByLink(String link) {
        for (var el : storage) {
            if (el.link().equals(link)) {
                return el;
            }
        }
        return null;
    }

    @Override
    public boolean existsByLink(String link) {
        for (var el : storage) {
            if (el.link().equals(link)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean deleteLinkByUserIdAndLink(long userId, String link) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).userId() == userId
                && storage.get(i).link().equals(link)) {
                storage.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean save(Link link) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).link().equals(link.link()) && storage.get(i).userId() == link.userId()) {
                return false;
            }
        }
        storage.add(link);
        return true;
    }

}

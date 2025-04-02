package backend.academy.scrapper.data;

import backend.academy.scrapper.model.entities.Tag;
import java.util.Optional;

public interface TagRepository {
    Optional<Tag> findByTag(String tag);

    Tag save(Tag tag);
}

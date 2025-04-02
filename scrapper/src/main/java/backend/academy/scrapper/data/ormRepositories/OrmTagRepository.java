package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.data.TagRepository;
import backend.academy.scrapper.model.entities.Tag;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public interface OrmTagRepository extends JpaRepository<Tag, Long>, TagRepository {
    Optional<Tag> findByTag(String tag);
}

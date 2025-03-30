package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.model.entities.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmTagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTag(String tag);
}

package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.data.FilterRepository;
import backend.academy.scrapper.model.entities.Filter;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "ORM", matchIfMissing = true)
public interface OrmFilterRepository extends JpaRepository<Filter, Long>, FilterRepository {
    Optional<Filter> findByFilter(String filter);
}

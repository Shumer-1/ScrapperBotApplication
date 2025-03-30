package backend.academy.scrapper.data.ormRepositories;

import backend.academy.scrapper.model.entities.Filter;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrmFilterRepository extends JpaRepository<Filter, Long> {
    Optional<Filter> findByFilter(String filter);
}

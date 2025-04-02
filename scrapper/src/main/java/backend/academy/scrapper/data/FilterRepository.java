package backend.academy.scrapper.data;

import backend.academy.scrapper.model.entities.Filter;
import java.util.Optional;

public interface FilterRepository {
    Optional<Filter> findByFilter(String filter);

    Filter save(Filter filter);
}

package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.FilterRepository;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.queries.FilterQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcFilterRepository implements FilterRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public JdbcFilterRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    private Filter mapFilter(ResultSet rs) throws SQLException {
        Filter filter = new Filter();
        filter.setId(rs.getLong("id"));
        filter.setFilter(rs.getString("filter"));
        return filter;
    }

    private final RowMapper<Filter> filterRowMapper = (rs, temp) -> mapFilter(rs);

    @Override
    public Optional<Filter> findByFilter(String filterValue) {
        String sql = FilterQuery.FIND_BY_VALUE.getSql();
        Map<String, Object> params = Collections.singletonMap("filterValue", filterValue);
        List<Filter> filters = namedJdbcTemplate.query(sql, params, filterRowMapper);
        return filters.stream().findFirst();
    }

    @Override
    public Filter save(Filter filter) {
        String sql = FilterQuery.SAVE_FILTER.getSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> params = new HashMap<>();
        params.put("filter", filter.getFilter());
        namedJdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder, new String[] {"id"});
        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        filter.setId(generatedKey.longValue());
        return filter;
    }
}

package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.model.entities.Filter;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcFilterRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcFilterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Filter> filterRowMapper = (rs, rowNum) -> {
        Filter filter = new Filter();
        filter.setId(rs.getLong("id"));
        filter.setFilter(rs.getString("filter"));
        return filter;
    };

    public Optional<Filter> findByFilter(String filterValue) {
        String sql = "SELECT * FROM filter WHERE filter = ?";
        List<Filter> filters = jdbcTemplate.query(sql, filterRowMapper, filterValue);
        return filters.isEmpty() ? Optional.empty() : Optional.of(filters.get(0));
    }

    public Filter save(Filter filter) {
        String sql = "INSERT INTO filter (filter) VALUES (?) RETURNING id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, filter.getFilter());
                    return ps;
                },
                keyHolder);
        filter.setId(keyHolder.getKey().longValue());
        return filter;
    }
}

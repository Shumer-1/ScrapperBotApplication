package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.model.entities.Filter;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcFilterRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(JdbcFilterRepository.class);
    private static final String INSERT_SQL = "INSERT INTO filter (filter) VALUES (?) RETURNING id";


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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            connection -> {
                PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] {"id"});
                try {
                    ps.setString(1, filter.getFilter());
                    return ps;
                } catch (Exception e) {
                    try {
                        ps.close();
                    } catch (Exception closeEx) {
                        log.error("Ошибка с PreparedStatement: {}", closeEx.getMessage());
                    }
                    throw e;
                }
            },
            keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        filter.setId(key.longValue());
        return filter;
    }

}

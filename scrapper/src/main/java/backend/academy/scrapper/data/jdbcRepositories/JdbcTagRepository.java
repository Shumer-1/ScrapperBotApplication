package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.TagRepository;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.queries.TagQuery;
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
public class JdbcTagRepository implements TagRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public JdbcTagRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    private Tag mapTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setTag(rs.getString("tag"));
        return tag;
    }

    private final RowMapper<Tag> tagRowMapper = (rs, _) -> mapTag(rs);

    @Override
    public Optional<Tag> findByTag(String tagName) {
        String sql = TagQuery.FIND_BY_VALUE.getSql();
        Map<String, Object> params = Collections.singletonMap("tagValue", tagName);
        List<Tag> tags = namedJdbcTemplate.query(sql, params, tagRowMapper);
        return tags.stream().findFirst();
    }

    @Override
    public Tag save(Tag tag) {
        String sql = TagQuery.SAVE_TAG.getSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> params = new HashMap<>();
        params.put("tag", tag.getTag());
        namedJdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder, new String[]{"id"});
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        tag.setId(keyHolder.getKey().longValue());
        return tag;
    }
}

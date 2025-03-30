package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.model.entities.Tag;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTagRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setTag(rs.getString("tag"));
        return tag;
    };

    public Optional<Tag> findByTag(String tagName) {
        String sql = "SELECT * FROM tags WHERE tag = ?";
        List<Tag> tags = jdbcTemplate.query(sql, tagRowMapper, tagName);
        return tags.isEmpty() ? Optional.empty() : Optional.of(tags.get(0));
    }

    public Tag save(Tag tag) {
        String sql = "INSERT INTO tags (tag) VALUES (?) RETURNING id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, tag.getTag());
                    return ps;
                },
                keyHolder);
        tag.setId(keyHolder.getKey().longValue());
        return tag;
    }
}

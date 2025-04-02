package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.LinkRepository;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcLinkRepository implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(JdbcLinkRepository.class);
    private static final String SAVE_SQL = "INSERT INTO tracking_link (link, user_id) VALUES (?, ?) RETURNING id";

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    private final RowMapper<Tag> tagRowMapper = (rs, rn) -> {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setTag(rs.getString("tag"));
        return tag;
    };

    private final RowMapper<Filter> filterRowMapper = (rs, rn) -> {
        Filter filter = new Filter();
        filter.setId(rs.getLong("id"));
        filter.setFilter(rs.getString("filter"));
        return filter;
    };

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("u_id"));
        user.setTelegramId(rs.getLong("u_tg_id"));
        user.setUsername(rs.getString("u_username"));
        return user;
    }

    private Link mapLink(ResultSet rs) throws SQLException {
        Link link = new Link();
        link.setId(rs.getLong("l_id"));
        link.setLink(rs.getString("l_link"));
        Timestamp ts = rs.getTimestamp("l_last_updated");
        link.setLastUpdated(ts != null ? ts.toInstant() : null);
        link.setUser(mapUser(rs));
        return link;
    }

    private List<Tag> getTagsForLink(long linkId) {
        String sql = "SELECT t.id, t.tag FROM tags t " + "JOIN link_and_tags lt ON t.id = lt.tag_id "
                + "WHERE lt.link_id = :linkId";
        Map<String, Object> params = Collections.singletonMap("linkId", linkId);
        return namedJdbcTemplate.query(sql, params, tagRowMapper);
    }

    private List<Filter> getFiltersForLink(long linkId) {
        String sql = "SELECT f.id, f.filter FROM filter f " + "JOIN link_and_filters lf ON f.id = lf.filter_id "
                + "WHERE lf.link_id = :linkId";
        Map<String, Object> params = Collections.singletonMap("linkId", linkId);
        return namedJdbcTemplate.query(sql, params, filterRowMapper);
    }

    @Override
    public List<Link> findByUserTelegramId(long telegramId) {
        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
                + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
                + "FROM tracking_link l "
                + "JOIN users u ON l.user_id = u.id "
                + "WHERE u.tg_id = ?";
        List<Link> links = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Link link = mapLink(rs);
                    link.setTags(new HashSet<>(getTagsForLink(link.getId())));
                    link.setFilters(new HashSet<>(getFiltersForLink(link.getId())));
                    return link;
                },
                telegramId);
        return links;
    }

    public Link findByUserIdAndLink(Long telegramId, String linkUrl) {
        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
                + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
                + "FROM tracking_link l "
                + "JOIN users u ON l.user_id = u.id "
                + "WHERE u.tg_id = :telegramId AND l.link = :linkUrl";
        Map<String, Object> params = new HashMap<>();
        params.put("telegramId", telegramId);
        params.put("linkUrl", linkUrl);
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, rowNum) -> mapLink(rs));
        return links.isEmpty() ? null : links.get(0);
    }

    @Override
    public Link saveLink(Link link) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(SAVE_SQL, new String[] {"id"});
                    try {
                        ps.setString(1, link.getLink());
                        ps.setLong(2, link.getUser().getId());
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
        link.setId(key.longValue());

        if (link.getTags() != null) {
            String sqlTag = "INSERT INTO link_and_tags (link_id, tag_id) VALUES (:linkId, :tagId)";
            for (Tag tag : link.getTags()) {
                Map<String, Object> params = new HashMap<>();
                params.put("linkId", link.getId());
                params.put("tagId", tag.getId());
                namedJdbcTemplate.update(sqlTag, params);
            }
        }
        if (link.getFilters() != null) {
            String sqlFilter = "INSERT INTO link_and_filters (link_id, filter_id) VALUES (:linkId, :filterId)";
            for (Filter filter : link.getFilters()) {
                Map<String, Object> params = new HashMap<>();
                params.put("linkId", link.getId());
                params.put("filterId", filter.getId());
                namedJdbcTemplate.update(sqlFilter, params);
            }
        }

        return link;
    }

    public void delete(Link link) {
        Map<String, Object> params = Collections.singletonMap("linkId", link.getId());
        namedJdbcTemplate.update("DELETE FROM link_and_tags WHERE link_id = :linkId", params);
        namedJdbcTemplate.update("DELETE FROM link_and_filters WHERE link_id = :linkId", params);
        namedJdbcTemplate.update("DELETE FROM tracking_link WHERE id = :linkId", params);
    }

    @Override
    public Page<Link> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM tracking_link";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
                + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
                + "FROM tracking_link l "
                + "JOIN users u ON l.user_id = u.id "
                + "ORDER BY l.id LIMIT :limit OFFSET :offset";
        Map<String, Object> params = new HashMap<>();
        params.put("limit", pageable.getPageSize());
        params.put("offset", (int) pageable.getOffset());
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, rowNum) -> mapLink(rs));

        return new PageImpl<>(links, pageable, total != null ? total : 0);
    }

    public void refreshLastUpdated(String linkName, long userTelegramId, Instant time) {
        String sql = "UPDATE tracking_link tl SET last_updated = :time " + "FROM users u "
                + "WHERE tl.user_id = u.id AND u.tg_id = :userTelegramId AND tl.link = :linkName";
        Map<String, Object> params = new HashMap<>();
        params.put("time", Timestamp.from(time));
        params.put("userTelegramId", userTelegramId);
        params.put("linkName", linkName);
        namedJdbcTemplate.update(sql, params);
    }

    @Override
    public List<Link> findByTelegramIdAndTag(Tag tag, long telegramId) {
        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
                + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
                + "FROM tracking_link l "
                + "JOIN users u ON l.user_id = u.id "
                + "JOIN link_and_tags lt ON l.id = lt.link_id "
                + "JOIN tags t ON lt.tag_id = t.id "
                + "WHERE u.tg_id = :telegramId AND t.id = :tagId";
        Map<String, Object> params = new HashMap<>();
        params.put("telegramId", telegramId);
        params.put("tagId", tag.getId());
        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> mapLink(rs));
    }
}

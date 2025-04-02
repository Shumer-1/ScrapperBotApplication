package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcLinkRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(JdbcLinkRepository.class);
    private static final String SAVE_SQL = "INSERT INTO tracking_link (link, user_id) VALUES (?, ?) RETURNING id";

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Link> linkRowMapper = (rs, rowNum) -> {
        Link link = new Link();
        link.setId(rs.getLong("id"));
        link.setLink(rs.getString("link"));
        return link;
    };

    public List<Link> findByUserId(Long telegramId) {
        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
            + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
            + "FROM tracking_link l "
            + "JOIN users u ON l.user_id = u.id "
            + "WHERE u.tg_id = ?";

        List<Link> links = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("u_id"));
                user.setTelegramId(rs.getLong("u_tg_id"));
                user.setUsername(rs.getString("u_username"));

                Link link = new Link();
                link.setId(rs.getLong("l_id"));
                link.setLink(rs.getString("l_link"));
                link.setLastUpdated(
                    rs.getTimestamp("l_last_updated") != null
                        ? rs.getTimestamp("l_last_updated").toInstant()
                        : null);
                link.setUser(user);

                String tagSql = "SELECT t.id, t.tag " + "FROM tags t "
                    + "JOIN link_and_tags lt ON t.id = lt.tag_id "
                    + "WHERE lt.link_id = ?";
                List<Tag> tags = jdbcTemplate.query(
                    tagSql,
                    (r, rn) -> {
                        Tag tag = new Tag();
                        tag.setId(r.getLong("id"));
                        tag.setTag(r.getString("tag"));
                        return tag;
                    },
                    link.getId());
                link.setTags(new HashSet<>(tags));

                String filterSql = "SELECT f.id, f.filter " + "FROM filter f "
                    + "JOIN link_and_filters lf ON f.id = lf.filter_id "
                    + "WHERE lf.link_id = ?";
                List<Filter> filters = jdbcTemplate.query(
                    filterSql,
                    (r, rn) -> {
                        Filter filter = new Filter();
                        filter.setId(r.getLong("id"));
                        filter.setFilter(r.getString("filter"));
                        return filter;
                    },
                    link.getId());
                link.setFilters(new HashSet<>(filters));

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
            + "WHERE u.tg_id = ? AND l.link = ?";
        List<Link> links = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("u_id"));
                user.setTelegramId(rs.getLong("u_tg_id"));
                user.setUsername(rs.getString("u_username"));

                Link link = new Link();
                link.setId(rs.getLong("l_id"));
                link.setLink(rs.getString("l_link"));
                link.setLastUpdated(
                    rs.getTimestamp("l_last_updated") != null
                        ? rs.getTimestamp("l_last_updated").toInstant()
                        : null);
                link.setUser(user);
                return link;
            },
            telegramId,
            linkUrl);
        return links.isEmpty() ? null : links.get(0);
    }

    public void save(Link link) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
            connection -> {
                PreparedStatement ps = connection.prepareStatement(SAVE_SQL, new String[]{"id"});
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
            for (var tag : link.getTags()) {
                String sqlTag = "INSERT INTO link_and_tags (link_id, tag_id) VALUES (?, ?)";
                jdbcTemplate.update(sqlTag, link.getId(), tag.getId());
            }
        }
        if (link.getFilters() != null) {
            for (var filter : link.getFilters()) {
                String sqlFilter = "INSERT INTO link_and_filters (link_id, filter_id) VALUES (?, ?)";
                jdbcTemplate.update(sqlFilter, link.getId(), filter.getId());
            }
        }
    }


    public void delete(Link link) {
        String deleteTags = "DELETE FROM link_and_tags WHERE link_id = ?";
        jdbcTemplate.update(deleteTags, link.getId());
        String deleteFilters = "DELETE FROM link_and_filters WHERE link_id = ?";
        jdbcTemplate.update(deleteFilters, link.getId());
        String sql = "DELETE FROM tracking_link WHERE id = ?";
        jdbcTemplate.update(sql, link.getId());
    }

    public Page<Link> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM tracking_link";
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class);

        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
            + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
            + "FROM tracking_link l "
            + "JOIN users u ON l.user_id = u.id "
            + "ORDER BY l.id LIMIT ? OFFSET ?";

        List<Link> links = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("u_id"));
                user.setTelegramId(rs.getLong("u_tg_id"));
                user.setUsername(rs.getString("u_username"));

                Link link = new Link();
                link.setId(rs.getLong("l_id"));
                link.setLink(rs.getString("l_link"));
                link.setLastUpdated(
                    rs.getTimestamp("l_last_updated") != null
                        ? rs.getTimestamp("l_last_updated").toInstant()
                        : null);
                link.setUser(user);

                return link;
            },
            pageable.getPageSize(),
            (int) pageable.getOffset());

        return new PageImpl<>(links, pageable, total != null ? total : 0);
    }

    public void refreshLastUpdated(String linkName, long userTelegramId, Instant time) {
        String sql = "UPDATE tracking_link tl " + "SET last_updated = ? "
            + "FROM users u "
            + "WHERE tl.user_id = u.id "
            + "  AND u.tg_id = ? "
            + "  AND tl.link = ?";
        Timestamp timestamp = Timestamp.from(time);
        jdbcTemplate.update(sql, timestamp, userTelegramId, linkName);
    }

    public List<Link> findByTagAndUserId(Long telegramId, Tag tag) {
        String sql = "SELECT l.id AS l_id, l.link AS l_link, l.last_updated AS l_last_updated, "
            + "u.id AS u_id, u.tg_id AS u_tg_id, u.username AS u_username "
            + "FROM tracking_link l "
            + "JOIN users u ON l.user_id = u.id "
            + "JOIN link_and_tags lt ON l.id = lt.link_id "
            + "JOIN tags t ON lt.tag_id = t.id "
            + "WHERE u.tg_id = ? AND t.id = ?";
        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("u_id"));
                user.setTelegramId(rs.getLong("u_tg_id"));
                user.setUsername(rs.getString("u_username"));

                Link link = new Link();
                link.setId(rs.getLong("l_id"));
                link.setLink(rs.getString("l_link"));
                link.setLastUpdated(
                    rs.getTimestamp("l_last_updated") != null
                        ? rs.getTimestamp("l_last_updated").toInstant()
                        : null);
                link.setUser(user);

                return link;
            },
            telegramId,
            tag.getId());
    }
}

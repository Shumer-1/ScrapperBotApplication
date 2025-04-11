package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.LinkRepository;
import backend.academy.scrapper.model.entities.Filter;
import backend.academy.scrapper.model.entities.Link;
import backend.academy.scrapper.model.entities.Tag;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.model.queries.FilterQuery;
import backend.academy.scrapper.model.queries.LinkQuery;
import backend.academy.scrapper.model.queries.TagQuery;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcLinkRepository implements LinkRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public JdbcLinkRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> {
        Tag tag = new Tag();
        tag.setId(rs.getLong("tag_id"));
        tag.setTag(rs.getString("tag_value"));
        return tag;
    };

    private final RowMapper<Filter> filterRowMapper = (rs, rowNum) -> {
        Filter filter = new Filter();
        filter.setId(rs.getLong("filter_id"));
        filter.setFilter(rs.getString("filter_value"));
        return filter;
    };

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setTelegramId(rs.getLong("user_telegram_id"));
        user.setUsername(rs.getString("user_username"));
        return user;
    }


    private Link mapLink(ResultSet rs) throws SQLException {
        Link link = new Link();
        link.setId(rs.getLong("tracking_link_id"));
        link.setLink(rs.getString("tracking_link_url"));
        Timestamp ts = rs.getTimestamp("tracking_link_last_updated");
        link.setLastUpdated(ts != null ? ts.toInstant() : null);
        link.setUser(mapUser(rs));
        return link;
    }

    private List<Tag> getTagsForLink(long linkId) {
        String sql = TagQuery.GET_TAGS_FOR_TRACKING_LINK.getSql();
        Map<String, Object> params = Collections.singletonMap("linkId", linkId);
        return namedJdbcTemplate.query(sql, params, tagRowMapper);
    }

    private List<Filter> getFiltersForLink(long linkId) {
        String sql = FilterQuery.GET_FILTERS_FOR_TRACKING_LINK.getSql();
        Map<String, Object> params = Collections.singletonMap("linkId", linkId);
        return namedJdbcTemplate.query(sql, params, filterRowMapper);
    }

    @Override
    public List<Link> findByUserTelegramId(long telegramId) {
        String sql = LinkQuery.FIND_BY_TELEGRAM_ID.getSql();
        Map<String, Object> params = Collections.singletonMap("telegramId", telegramId);
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, _) -> {
            Link link = mapLink(rs);
            link.setTags(new HashSet<>(getTagsForLink(link.getId())));
            link.setFilters(new HashSet<>(getFiltersForLink(link.getId())));
            return link;
        });
        return links;
    }

    @Override
    public Link findByUserIdAndLink(Long telegramId, String linkUrl) {
        String sql = LinkQuery.FIND_BY_TELEGRAM_ID_AND_LINK.getSql();
        Map<String, Object> params = new HashMap<>();
        params.put("telegramId", telegramId);
        params.put("linkUrl", linkUrl);
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, _) -> {
            Link link = mapLink(rs);
            link.setTags(new HashSet<>(getTagsForLink(link.getId())));
            link.setFilters(new HashSet<>(getFiltersForLink(link.getId())));
            return link;
        });
        return links.isEmpty() ? null : links.get(0);
    }

    @Override
    public Link saveLink(Link link) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> params = new HashMap<>();
        params.put("link", link.getLink());
        params.put("userId", link.getUser().getId());
        namedJdbcTemplate.update(
            LinkQuery.SAVE_LINK.getSql(),
            new MapSqlParameterSource(params),
            keyHolder,
            new String[]{"id"}
        );
        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        link.setId(keyHolder.getKey().longValue());
        saveAssociationTag(link);
        saveAssociationFilter(link);
        return link;
    }

    private void saveAssociationTag(Link link) {
        if (link.getTags() != null && !link.getTags().isEmpty()) {
            String sqlTag = LinkQuery.SAVE_LINK_AND_TAG.getSql();
            link.getTags().forEach(tag -> {
                Map<String, Object> tagParams = new HashMap<>();
                tagParams.put("linkId", link.getId());
                tagParams.put("tagId", tag.getId());
                namedJdbcTemplate.update(sqlTag, tagParams);
            });
        }
    }

    private void saveAssociationFilter(Link link){
        if (link.getFilters() != null && !link.getFilters().isEmpty()) {
            String sqlFilter = LinkQuery.SAVE_LINK_AND_FILTER.getSql();
            link.getFilters().forEach(filter -> {
                Map<String, Object> filterParams = new HashMap<>();
                filterParams.put("linkId", link.getId());
                filterParams.put("filterId", filter.getId());
                namedJdbcTemplate.update(sqlFilter, filterParams);
            });
        }
    }

    @Override
    public void delete(Link link) {
        Map<String, Object> params = Collections.singletonMap("linkId", link.getId());
        namedJdbcTemplate.update(LinkQuery.DELETE_LINK_AND_TAGS.getSql(), params);
        namedJdbcTemplate.update(LinkQuery.DELETE_LINK_AND_FILTERS.getSql(), params);
        namedJdbcTemplate.update(LinkQuery.DELETE_TRACKING_LINK.getSql(), params);
    }

    @Override
    public Page<Link> findAll(Pageable pageable) {
        String countSql = LinkQuery.COUNT_ALL.getSql();
        Integer total = namedJdbcTemplate.queryForObject(countSql, Collections.emptyMap(), Integer.class);
        String sql = LinkQuery.FIND_ALL.getSql();
        Map<String, Object> params = new HashMap<>();
        params.put("limit", pageable.getPageSize());
        params.put("offset", (int) pageable.getOffset());
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Link link = mapLink(rs);
            link.setTags(new HashSet<>(getTagsForLink(link.getId())));
            link.setFilters(new HashSet<>(getFiltersForLink(link.getId())));
            return link;
        });
        return new PageImpl<>(links, pageable, total != null ? total : 0);
    }

    @Override
    public void refreshLastUpdated(String linkName, long userTelegramId, Instant time) {
        String sql = LinkQuery.REFRESH_LAST_UPDATED.getSql();
        Map<String, Object> params = new HashMap<>();
        params.put("time", Timestamp.from(time));
        params.put("userTelegramId", userTelegramId);
        params.put("linkName", linkName);
        namedJdbcTemplate.update(sql, params);
    }

    @Override
    public List<Link> findByTelegramIdAndTag(Tag tag, long telegramId) {
        String sql = LinkQuery.FIND_BY_TELEGRAM_AND_TAG.getSql();
        Map<String, Object> params = new HashMap<>();
        params.put("telegramId", telegramId);
        params.put("tagId", tag.getId());
        List<Link> links = namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            Link link = mapLink(rs);
            link.setTags(new HashSet<>(getTagsForLink(link.getId())));
            link.setFilters(new HashSet<>(getFiltersForLink(link.getId())));
            return link;
        });
        return links;
    }
}

package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.UserRepository;
import backend.academy.scrapper.model.entities.User;
import backend.academy.scrapper.model.queries.UserQuery;
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
public class JdbcUserRepository implements UserRepository {

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public JdbcUserRepository(NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setTelegramId(rs.getLong("tg_id"));
        user.setUsername(rs.getString("username"));
        return user;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> mapUser(rs);

    @Override
    public Optional<User> findByTelegramId(Long telegramId) {
        String sql = UserQuery.FIND_BY_TELEGRAM_ID.getSql();
        Map<String, Object> params = Collections.singletonMap("telegramId", telegramId);
        List<User> users = namedJdbcTemplate.query(sql, params, userRowMapper);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public User saveUser(User user) {
        if (findByTelegramId(user.getTelegramId()).isPresent()) {
            return user;
        }
        String sql = UserQuery.SAVE_USER.getSql();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> params = new HashMap<>();
        params.put("telegramId", user.getTelegramId());
        params.put("username", user.getUsername());
        namedJdbcTemplate.update(sql, new MapSqlParameterSource(params), keyHolder, new String[] {"id"});
        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        user.setId(generatedKey.longValue());
        return user;
    }
}

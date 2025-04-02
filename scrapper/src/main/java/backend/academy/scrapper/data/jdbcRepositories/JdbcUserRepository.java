package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.data.UserRepository;
import backend.academy.scrapper.model.entities.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "access-type", havingValue = "SQL")
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(JdbcUserRepository.class);

    private static final String FIND_BY_TELEGRAM_ID_SQL = "SELECT * FROM users WHERE tg_id = ?";
    private static final String SAVE_SQL = "INSERT INTO users (tg_id, username) VALUES (?, ?) RETURNING id";

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setTelegramId(rs.getLong("tg_id"));
        user.setUsername(rs.getString("username"));
        return user;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> mapUser(rs);

    public Optional<User> findByTelegramId(Long telegramId) {
        List<User> users = jdbcTemplate.query(FIND_BY_TELEGRAM_ID_SQL, userRowMapper, telegramId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public User saveUser(User user) {
        if (findByTelegramId(user.getTelegramId()).isPresent()) {
            return user;
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> createPreparedStatement(connection, user), keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный идентификатор");
        }
        user.setId(key.longValue());
        return user;
    }

    private PreparedStatement createPreparedStatement(java.sql.Connection connection, User user) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SAVE_SQL, new String[] {"id"});
        try {
            ps.setLong(1, user.getTelegramId());
            ps.setString(2, user.getUsername());
            return ps;
        } catch (Exception e) {
            try {
                ps.close();
            } catch (Exception closeEx) {
                log.error("Ошибка с PreparedStatement: {}", closeEx.getMessage());
            }
            throw e;
        }
    }
}

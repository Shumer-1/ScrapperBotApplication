package backend.academy.scrapper.data.jdbcRepositories;

import backend.academy.scrapper.model.entities.User;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setTelegramId(rs.getLong("tg_id"));
        user.setUsername(rs.getString("username"));
        // Ссылки можно подгружать отдельным запросом, если потребуется
        return user;
    };

    public Optional<User> findByTelegramId(Long telegramId) {
        String sql = "SELECT * FROM users WHERE tg_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, telegramId);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public void save(User user) {
        if (findByTelegramId(user.getTelegramId()).isPresent()) {
            return;
        }
        String sql = "INSERT INTO users (tg_id, username) VALUES (?, ?) RETURNING id";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setLong(1, user.getTelegramId());
                    ps.setString(2, user.getUsername());
                    return ps;
                },
                keyHolder);
        user.setId(keyHolder.getKey().longValue());
    }
}

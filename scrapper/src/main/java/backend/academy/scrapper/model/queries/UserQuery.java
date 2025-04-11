package backend.academy.scrapper.model.queries;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum UserQuery {
    FIND_BY_TELEGRAM_ID("sql/userQueries/selectQueries/SelectUserByTelegramId.sql"),
    SAVE_USER("sql/userQueries/insertQueries/InsertUser.sql");

    private final String sql;

    UserQuery(String resourcePath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("SQL file not found: " + resourcePath);
            }
            this.sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading SQL file: " + resourcePath, e);
        }
    }

    public String getSql() {
        return sql;
    }
}

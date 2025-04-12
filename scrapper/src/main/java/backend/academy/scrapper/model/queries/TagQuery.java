package backend.academy.scrapper.model.queries;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum TagQuery {
    GET_TAGS_FOR_TRACKING_LINK("sql/tagQueries/selectQueries/SelectTagsForTrackingLink.sql"),
    FIND_BY_VALUE("sql/tagQueries/selectQueries/SelectTagByValue.sql"),
    SAVE_TAG("sql/tagQueries/insertQueries/InsertTag.sql");

    private final String sql;

    TagQuery(String resourcePath) {
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

package backend.academy.scrapper.model.queries;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum FilterQuery {
    GET_FILTERS_FOR_TRACKING_LINK("sql/filterQueries/selectQueries/SelectFiltersForTrackingLink.sql"),
    FIND_BY_VALUE("sql/filterQueries/selectQueries/SelectFilterByFilterValue.sql"),
    SAVE_FILTER("sql/filterQueries/insertQueries/InsertFilter.sql");

    private final String sql;

    FilterQuery(String resourcePath) {
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

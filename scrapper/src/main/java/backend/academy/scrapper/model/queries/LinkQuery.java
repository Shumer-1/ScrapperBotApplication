package backend.academy.scrapper.model.queries;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public enum LinkQuery {
    SAVE_LINK_AND_TAG("sql/linkQueries/insertQueries/InsertLinkAndTag.sql"),
    SAVE_LINK_AND_FILTER("sql/linkQueries/insertQueries/InsertLinkAndFilter.sql"),
    FIND_BY_TELEGRAM_ID("sql/linkQueries/selectQueries/SelectTrackingLinksByTelegramId.sql"),
    FIND_BY_TELEGRAM_ID_AND_LINK("sql/linkQueries/selectQueries/SelectTrackingLinkByTelegramIdAndLink.sql"),
    SAVE_LINK("sql/linkQueries/insertQueries/InsertTrackingLink.sql"),
    COUNT_ALL("sql/linkQueries/countQueries/CountTrackingLinks.sql"),
    FIND_ALL("sql/linkQueries/selectQueries/SelectAllTrackingLinks.sql"),
    REFRESH_LAST_UPDATED("sql/linkQueries/updateQueries/RefreshTrackingLinkLastUpdated.sql"),
    FIND_BY_TELEGRAM_AND_TAG("sql/linkQueries/selectQueries/SelectTrackingLinksByTelegramIdAndTag.sql"),
    DELETE_LINK_AND_TAGS("sql/linkQueries/deleteQueries/DeleteLinkAndTags.sql"),
    DELETE_LINK_AND_FILTERS("sql/linkQueries/deleteQueries/DeleteLinkAndFilters.sql"),
    DELETE_TRACKING_LINK("sql/linkQueries/deleteQueries/DeleteTrackingLink.sql");

    private final String sql;

    LinkQuery(String resourcePath) {
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

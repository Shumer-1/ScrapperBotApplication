package backend.academy.scrapper.data;

import backend.academy.scrapper.model.TrackingData;
import java.time.Instant;
import java.util.Collection;

public interface TrackingRepository {
    void addTracking(TrackingData trackingData);

    void removeTracking(TrackingData trackingData);

    Collection<TrackingData> getAllTracking();

    void updateLastUpdated(String link, long userId, Instant lastUpdated);
}

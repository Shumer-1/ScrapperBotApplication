package backend.academy.scrapper.data;

import backend.academy.scrapper.model.TrackingData;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTrackingRepository implements TrackingRepository {

    private final Map<Long, List<TrackingData>> trackingMap = new ConcurrentHashMap<>();

    @Override
    public void addTracking(TrackingData trackingData) {
        trackingMap
                .computeIfAbsent(trackingData.getUserId(), id -> new CopyOnWriteArrayList<>())
                .add(trackingData);
    }

    @Override
    public void removeTracking(TrackingData trackingData) {
        List<TrackingData> list = trackingMap.get(trackingData.getUserId());
        if (list != null) {
            list.removeIf(td -> td.getLink().equals(trackingData.getLink()));
        }
    }

    @Override
    public Collection<TrackingData> getAllTracking() {
        return trackingMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public void refreshLastUpdated(String link, long userId, Instant lastUpdated) {
        List<TrackingData> list = trackingMap.get(userId);
        if (list != null) {
            for (TrackingData trackingData : list) {
                if (trackingData.getLink().equals(link)) {
                    trackingData.setLastUpdated(lastUpdated);
                    break;
                }
            }
        }
    }
}

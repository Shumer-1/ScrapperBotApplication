package backend.academy.bot.states;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TrackStateManager {
    private final Map<Long, TrackCommandState> stateMap = new ConcurrentHashMap<>();

    public void startTracking(long userId, String link) {
        stateMap.put(userId, new TrackCommandState(link));
    }

    public TrackCommandState getState(long userId) {
        return stateMap.get(userId);
    }

    public void clearState(long userId) {
        stateMap.remove(userId);
    }
}


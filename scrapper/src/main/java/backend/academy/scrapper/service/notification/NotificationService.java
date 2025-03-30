package backend.academy.scrapper.service.notification;

import org.springframework.stereotype.Service;

@Service
public interface NotificationService {
    void sendNotification(String message, long userId);
}

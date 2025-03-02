package backend.academy.scrapper.model;

public class NotificationRequest {
    private String message;
    private long userId;

    public NotificationRequest() {}

    public NotificationRequest(String message, long userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
